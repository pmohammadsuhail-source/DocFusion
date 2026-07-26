package com.example.data.service

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.BlockType
import com.example.data.local.DocumentBlock
import com.example.data.local.DocumentEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExportManager {

    fun generatePdf(context: Context, document: DocumentEntity): File {
        val pdfDocument = PdfDocument()
        
        // Standard A4 dimensions in points (72 points per inch) -> 595 x 842 points
        val pageWidth = 595
        val pageHeight = 842
        val margin = 40
        val contentWidth = pageWidth - (margin * 2)

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Paints
        val titlePaint = Paint().apply {
            color = Color.parseColor("#1E3A8A")
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val metaPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 10f
            isAntiAlias = true
        }

        val h1Paint = Paint().apply {
            color = Color.parseColor("#1E3A8A")
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val h2Paint = Paint().apply {
            color = Color.parseColor("#2563EB")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 11f
            isAntiAlias = true
        }

        val quotePaint = Paint().apply {
            color = Color.parseColor("#1E40AF")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
        }

        var yPos = margin.toFloat()

        // Draw Document Title Header
        canvas.drawText(document.title, margin.toFloat(), yPos + 18f, titlePaint)
        yPos += 30f

        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val metaStr = "Author: ${document.authorName}  |  Last Updated: ${dateFormat.format(Date(document.updatedAt))}  |  DocFusion AI"
        canvas.drawText(metaStr, margin.toFloat(), yPos + 10f, metaPaint)
        yPos += 20f

        canvas.drawLine(margin.toFloat(), yPos, (pageWidth - margin).toFloat(), yPos, linePaint)
        yPos += 25f

        // Draw Content Blocks
        for (block in document.blocks) {
            // Check page overflow
            if (yPos > pageHeight - margin - 50) {
                // Draw Footer
                canvas.drawText("DocFusion Export  •  Page $pageNumber", margin.toFloat(), (pageHeight - 20).toFloat(), metaPaint)
                pdfDocument.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = margin.toFloat() + 20f
            }

            when (block.type) {
                BlockType.HEADING1 -> {
                    yPos += 10f
                    val textLines = wrapText(block.text, h1Paint, contentWidth.toFloat())
                    for (line in textLines) {
                        canvas.drawText(line, margin.toFloat(), yPos + 14f, h1Paint)
                        yPos += 22f
                    }
                    yPos += 6f
                }
                BlockType.HEADING2 -> {
                    yPos += 8f
                    val textLines = wrapText(block.text, h2Paint, contentWidth.toFloat())
                    for (line in textLines) {
                        canvas.drawText(line, margin.toFloat(), yPos + 12f, h2Paint)
                        yPos += 18f
                    }
                    yPos += 4f
                }
                BlockType.PARAGRAPH -> {
                    val pPaint = Paint(bodyPaint).apply {
                        if (block.isBold) typeface = Typeface.create(typeface, Typeface.BOLD)
                        if (block.isItalic) typeface = Typeface.create(typeface, Typeface.ITALIC)
                        try {
                            color = Color.parseColor(block.fontColorHex)
                        } catch (_: Exception) {}
                    }
                    val textLines = wrapText(block.text, pPaint, contentWidth.toFloat())
                    for (line in textLines) {
                        canvas.drawText(line, margin.toFloat(), yPos + 10f, pPaint)
                        yPos += 16f
                    }
                    yPos += 8f
                }
                BlockType.BULLET_LIST -> {
                    val bulletPaint = Paint(bodyPaint)
                    val textLines = wrapText("• " + block.text, bulletPaint, contentWidth - 15f)
                    for (i in textLines.indices) {
                        val indent = if (i == 0) margin.toFloat() else margin.toFloat() + 15f
                        canvas.drawText(textLines[i], indent, yPos + 10f, bulletPaint)
                        yPos += 16f
                    }
                    yPos += 4f
                }
                BlockType.NUMBERED_LIST -> {
                    val numPaint = Paint(bodyPaint)
                    val textLines = wrapText(block.text, numPaint, contentWidth - 15f)
                    for (i in textLines.indices) {
                        val prefix = if (i == 0) "1. " else "   "
                        canvas.drawText(prefix + textLines[i], margin.toFloat(), yPos + 10f, numPaint)
                        yPos += 16f
                    }
                    yPos += 4f
                }
                BlockType.QUOTE -> {
                    val qBgPaint = Paint().apply {
                        color = Color.parseColor("#EFF6FF")
                    }
                    canvas.drawRect(margin.toFloat(), yPos, (pageWidth - margin).toFloat(), yPos + 36f, qBgPaint)
                    canvas.drawLine(margin.toFloat(), yPos, margin.toFloat(), yPos + 36f, Paint().apply {
                        color = Color.parseColor("#2563EB")
                        strokeWidth = 3f
                    })
                    val textLines = wrapText(block.text, quotePaint, contentWidth - 20f)
                    for (line in textLines) {
                        canvas.drawText(line, margin.toFloat() + 12f, yPos + 22f, quotePaint)
                        yPos += 16f
                    }
                    yPos += 12f
                }
                BlockType.IMAGE -> {
                    // Draw clean image placeholder frame in PDF
                    val imgHeight = 120f
                    val imgRectPaint = Paint().apply {
                        color = Color.parseColor("#F1F5F9")
                        style = Paint.Style.FILL
                    }
                    val borderPaint = Paint().apply {
                        color = Color.parseColor("#CBD5E1")
                        style = Paint.Style.STROKE
                        strokeWidth = 1f
                    }
                    canvas.drawRoundRect(margin.toFloat(), yPos, (pageWidth - margin).toFloat(), yPos + imgHeight, 8f, 8f, imgRectPaint)
                    canvas.drawRoundRect(margin.toFloat(), yPos, (pageWidth - margin).toFloat(), yPos + imgHeight, 8f, 8f, borderPaint)
                    
                    canvas.drawText("[ Embedded Image ]", margin.toFloat() + (contentWidth / 2f) - 50f, yPos + (imgHeight / 2f) + 4f, metaPaint)
                    yPos += imgHeight + 12f
                }
            }
        }

        // Draw Footer on last page
        canvas.drawText("DocFusion Export  •  Page $pageNumber", margin.toFloat(), (pageHeight - 20).toFloat(), metaPaint)
        pdfDocument.finishPage(page)

        // Save PDF to exports directory
        val exportDir = File(context.filesDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val sanitizeTitle = document.title.replace("[^a-zA-Z0-9_]".toRegex(), "_")
        val pdfFile = File(exportDir, "${sanitizeTitle}_${System.currentTimeMillis()}.pdf")

        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isEmpty()) return listOf("")
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        return lines.ifEmpty { listOf(text) }
    }

    fun sharePdfFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share PDF Document"))
        } catch (_: Exception) {
            // Intent fallback
        }
    }
}
