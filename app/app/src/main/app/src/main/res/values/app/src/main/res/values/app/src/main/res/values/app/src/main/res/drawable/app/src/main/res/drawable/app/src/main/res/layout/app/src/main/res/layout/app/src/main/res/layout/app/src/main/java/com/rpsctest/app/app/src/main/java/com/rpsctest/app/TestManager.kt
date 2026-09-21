package com.rpsctest.app

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

class TestManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("test_library", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getTests(): List<TestItem> {
        val json = prefs.getString("tests", "[]")
        val type = object : TypeToken<List<TestItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun saveTests(tests: List<TestItem>) {
        prefs.edit().putString("tests", gson.toJson(tests)).apply()
    }

    fun importFile(uri: Uri): Boolean {
        val fileName = getFileName(uri) ?: "Unknown_Test"
        val testId = System.currentTimeMillis().toString()
        val destDir = File(context.filesDir, "tests/$testId")
        
        if (!destDir.exists()) destDir.mkdirs()

        val indexPath = if (fileName.endsWith(".zip", true)) {
            extractZip(uri, destDir)
        } else {
            copyHtml(uri, destDir)
        }

        if (indexPath != null) {
            val testName = fileName.substringBeforeLast(".")
            val testItem = TestItem(testId, testName, fileName, indexPath, System.currentTimeMillis())
            val currentTests = getTests().toMutableList()
            currentTests.add(0, testItem)
            saveTests(currentTests)
            return true
        }
        
        destDir.deleteRecursively()
        return false
    }

    private fun extractZip(uri: Uri, destDir: File): String? {
        var indexPath: String? = null
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val file = File(destDir, entry.name)
                    if (file.canonicalPath.startsWith(destDir.canonicalPath)) {
                        if (entry.isDirectory) {
                            file.mkdirs()
                        } else {
                            file.parentFile?.mkdirs()
                            FileOutputStream(file).use { fos ->
                                zis.copyTo(fos)
                            }
                            if (entry.name.endsWith("index.html", true) && indexPath == null) {
                                indexPath = file.absolutePath
                            }
                        }
                    }
                    entry = zis.nextEntry
                }
            }
        }
        return indexPath
    }

    private fun copyHtml(uri: Uri, destDir: File): String? {
        val file = File(destDir, "index.html")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    fun deleteTest(test: TestItem) {
        val dir = File(context.filesDir, "tests/${test.id}")
        if (dir.exists()) dir.deleteRecursively()
        val updatedList = getTests().filter { it.id != test.id }
        saveTests(updatedList)
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = cursor.getString(index)
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) result = result?.substring(cut + 1)
        }
        return result
    }
}
