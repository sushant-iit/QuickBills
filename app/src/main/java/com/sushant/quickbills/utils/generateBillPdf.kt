package com.sushant.quickbills.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.*
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.VerticalAlignment
import com.sushant.quickbills.model.Bill
import java.io.File
import java.io.FileNotFoundException


fun createBillPDF(context: Context, bill: Bill) {
    val storagePath = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val fileName = bill.customerDetails!!.number + "T" + billCreationDateAndTime(bill)
    val file = File(storagePath, "${fileName}.pdf")
    if(!file.exists())
        generatePDF(bill, file)
}

private fun generatePDF(bill: Bill, file:File){
    try {
        val pdfWriter = PdfWriter(file)
        val pdfDocument = PdfDocument(pdfWriter)
        pdfDocument.defaultPageSize = PageSize.A4
        val document = Document(pdfDocument)

        //Dividing document into two parts for adding top level entries------------------------------------
        val columnWidthTop = arrayOf(280.0F, 280.0F).toFloatArray()
        val tableHead = Table(columnWidthTop)
        //-------------------------------Main Heading of Invoice and Shop Name
        val invoiceHead =
            Paragraph().add("INVOICE").setFontSize(25f)
                .setBold()
        val brandName = Text("Quick Sweets\n").setBold().setFontSize(15F)
        val mobiLeHead = Text("M: ").setBold()
        val emailHead = Text("E: ").setBold()
        val addressHead = Text("A: ").setBold()
        val dateHead = Text("D: ").setBold()
        val brandMobileNo = Text("9971758038\n").setItalic()
        val brandEmail =
            Link(
                "krsushant.sk@gmail.com\n",
                PdfAction.createURI("mailto:krsushant.sk@gmail.com")
            ).setItalic()
        val brandAddress = Text("Madhubani, Bihar\n")
        val shopInfo =
            Paragraph().add(brandName)
                .add(mobiLeHead).add(brandMobileNo)
                .add(emailHead).add(brandEmail)
                .add(addressHead).add(brandAddress)
                .setTextAlignment(TextAlignment.RIGHT)
        //------------------------------Setting Invoice Details
        val invoiceToHead = Text("INVOICE TO:\n").setBold().setFontSize(10F)
        val customerName =
            Text("${bill.customerDetails!!.name}\n").setFontColor(DeviceRgb(11, 81, 89)).setBold()
                .setFontSize(15F)
        val mobileNo = Text("${bill.customerDetails!!.number}\n").setItalic()
        val customerAddress = Text("${bill.customerDetails!!.address}\n")
        val billDate = Text("${billCreationDateAndTime(bill)}\n")
        val leftParagraph = Paragraph()
            .add("\n").add(invoiceToHead).add(customerName).add(mobiLeHead).add(mobileNo)
            .add(addressHead)
            .add(customerAddress).add(dateHead).add(billDate)
        val totalAmountHead = Text("Total Due:\n").setBold()
        val totalAmount =
            Text("\u20B9 ${bill.totalAmount.toString()}").setUnderline()
                .setFontSize(25f)
        val rightParagraph =
            Paragraph().add("\n").add(totalAmountHead).add(totalAmount)
                .setTextAlignment(TextAlignment.RIGHT)
        tableHead.addCell(
            Cell().add(invoiceHead).setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER)
        )
        tableHead.addCell(
            Cell().add(shopInfo).setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER)
        )
        tableHead.addCell(Cell().add(leftParagraph).setBorder(Border.NO_BORDER))
        tableHead.addCell(Cell().add(rightParagraph).setBorder(Border.NO_BORDER))
        //-----------------------------------------------------------------------------------------

        //Adding Particulars-----------------------------------------------------------------------
        val columnWidthParticulars = arrayOf(65.88F, 197.64F, 98.82F, 98.82F, 98.82F).toFloatArray()
        val tableParticulars = Table(columnWidthParticulars)
        //--------------------------Setting up table heading-----
        val serialHead = Text("Sno.").setBold()
        val itemHead = Text("Item Description").setBold()
        val rateHead = Text("Unit Price").setBold()
        val quantityHead = Text("Qty").setBold()
        val amountHead = Text("Amount").setBold()
        tableParticulars.addCell(
            Cell().add(Paragraph().add(serialHead).setTextAlignment(TextAlignment.CENTER))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER)
                .setBorderTop(SolidBorder(1F))
                .setBorderBottom(SolidBorder(1F))
                .setBackgroundColor(DeviceRgb(231, 231, 231))
        )
        tableParticulars.addCell(
            Cell().add(Paragraph().add(itemHead).setTextAlignment(TextAlignment.LEFT))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER)
                .setBorderTop(SolidBorder(1F))
                .setBorderBottom(SolidBorder(1F))
                .setBackgroundColor(DeviceRgb(231, 231, 231))
        )
        tableParticulars.addCell(
            Cell().add(Paragraph().add(rateHead).setTextAlignment(TextAlignment.RIGHT))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER)
                .setBorderTop(SolidBorder(1F))
                .setBorderBottom(SolidBorder(1F))
        )
        tableParticulars.addCell(
            Cell().add(Paragraph().add(quantityHead).setTextAlignment(TextAlignment.RIGHT))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER)
                .setBorderTop(SolidBorder(1F))
                .setBorderBottom(SolidBorder(1F))
        )
        tableParticulars.addCell(
            Cell().add(Paragraph().add(amountHead).setTextAlignment(TextAlignment.RIGHT))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER)
                .setBorderTop(SolidBorder(1F))
                .setBorderBottom(SolidBorder(1F))
        )
        //Iterate through all the particulars and add to the pdf
        var count = 1
        for (item in bill.particulars!!) {
            val serial = Text(count.toString())
            val itemName = Text(item.itemName)
            val rate = Text(item.itemPrice.toString())
            val quantity = Text(item.itemQty.toString())
            val amount = Text(item.itemAmount.toString()).setBold()
            count++
            tableParticulars.addCell(
                Cell().add(Paragraph().add(serial).setTextAlignment(TextAlignment.CENTER))
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(Border.NO_BORDER)
            )
            tableParticulars.addCell(
                Cell().add(Paragraph().add(itemName).setTextAlignment(TextAlignment.LEFT))
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(Border.NO_BORDER)
            )
            tableParticulars.addCell(
                Cell().add(Paragraph().add(rate).setTextAlignment(TextAlignment.RIGHT))
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(Border.NO_BORDER)
                    .setBackgroundColor(DeviceRgb(231, 231, 231))
            )
            tableParticulars.addCell(
                Cell().add(Paragraph().add(quantity).setTextAlignment(TextAlignment.RIGHT))
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(Border.NO_BORDER)
                    .setBackgroundColor(DeviceRgb(231, 231, 231))
            )
            tableParticulars.addCell(
                Cell().add(Paragraph().add(amount).setTextAlignment(TextAlignment.RIGHT))
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(Border.NO_BORDER)
                    .setBackgroundColor(DeviceRgb(231, 231, 231))
            )
        }
        //Finally show the total amount
        tableParticulars.addCell(
            Cell(0, 4).add(
                Paragraph().add("Total Amount:").setBold()
                    .setTextAlignment(TextAlignment.RIGHT)
            )
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER)
                .setBorderTop(SolidBorder(1F))
                .setBorderBottom(SolidBorder(1F))
                .setBorderRight(SolidBorder(1F))
        )
        tableParticulars.addCell(
            Cell().add(
                Paragraph().add(bill.totalAmount.toString()).setBold()
                    .setTextAlignment(TextAlignment.RIGHT)
            )
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER)
                .setBorderTop(SolidBorder(1F))
                .setBorderBottom(SolidBorder(1F))
        )
        //-------------------------------------------------------------------------------------------

        //Setting Up Thank You Message---------------------------------------------------------------
        val thanksText = Text("Thank you for your business!\n").setFontColor(DeviceRgb(49, 62, 84))
            .setFontSize(15F)
        val concludeText = Text("Should you have any queries, please contact us.").setFontColor(
            DeviceRgb(
                49,
                62,
                84
            )
        )
            .setFontSize(8F)
        val thanksPara =
            Paragraph().add(thanksText).add(concludeText).setTextAlignment(TextAlignment.CENTER)

        document.add(tableHead)
        document.add(Paragraph().add("\n"))
        document.add(tableParticulars)
        document.add(Paragraph().add("\n"))
        document.add(thanksPara)
        document.close()
    } catch (e: FileNotFoundException) {
        Log.e("PdfCreateError", e.message.toString())
    }
}

