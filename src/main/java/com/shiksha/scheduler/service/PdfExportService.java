package com.shiksha.scheduler.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.shiksha.scheduler.model.Interview;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    private static final BaseColor HEADER_COLOR  = new BaseColor(30,  39,  73);
    private static final BaseColor ROW_ALT_COLOR = new BaseColor(245, 246, 250);
    private static final BaseColor ACCENT_BLUE   = new BaseColor(66, 135, 245);

    public byte[] generateInterviewReport(List<Interview> interviews) throws DocumentException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 28, 28, 40, 28);
        PdfWriter.getInstance(document, out);
        document.open();

        // ── Title ────────────────────────────────────────────────────────────────
        Font titleFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.WHITE);
        Font subFont    = FontFactory.getFont(FontFactory.HELVETICA,       9, new BaseColor(180, 190, 220));
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  9, BaseColor.WHITE);
        Font cellFont   = FontFactory.getFont(FontFactory.HELVETICA,       8, BaseColor.DARK_GRAY);
        Font boldFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  8, BaseColor.DARK_GRAY);

        PdfPTable titleBar = new PdfPTable(1);
        titleBar.setWidthPercentage(100);
        PdfPCell titleCell = new PdfPCell();
        titleCell.setBackgroundColor(HEADER_COLOR);
        titleCell.setPadding(14);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.addElement(new Paragraph("Smart Interview Scheduler", titleFont));
        titleCell.addElement(new Paragraph("Interview Report  •  Generated: " +
                java.time.LocalDate.now().format(DATE_FMT) + "  •  Total Records: " + interviews.size(), subFont));
        titleBar.addCell(titleCell);
        document.add(titleBar);
        document.add(Chunk.NEWLINE);

        // ── Table ────────────────────────────────────────────────────────────────
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.5f, 2f, 2f, 2f, 2f, 1.5f, 1.5f});

        String[] headers = {"#", "Candidate", "Email", "Job", "Interviewer", "Date & Time", "Status"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(ACCENT_BLUE);
            cell.setPadding(7);
            cell.setBorderColor(BaseColor.WHITE);
            cell.setBorderWidth(0.5f);
            table.addCell(cell);
        }

        for (int idx = 0; idx < interviews.size(); idx++) {
            Interview i = interviews.get(idx);
            BaseColor bg = (idx % 2 == 0) ? BaseColor.WHITE : ROW_ALT_COLOR;

            addCell(table, String.valueOf(idx + 1), cellFont, bg);
            addCell(table, i.getCandidate().getFullName(), boldFont, bg);
            addCell(table, i.getCandidate().getEmail(), cellFont, bg);
            addCell(table, i.getJob().getTitle(), cellFont, bg);
            addCell(table, i.getInterviewer().getFullName(), cellFont, bg);
            String dateTime = i.getAvailability().getAvailableDate().format(DATE_FMT)
                    + "\n" + i.getAvailability().getStartTime().format(TIME_FMT);
            addCell(table, dateTime, cellFont, bg);

            // Status cell with color
            Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8,
                    switch (i.getStatus()) {
                        case SCHEDULED   -> new BaseColor(30, 120, 255);
                        case COMPLETED   -> new BaseColor(34, 139, 34);
                        case CANCELLED   -> new BaseColor(220, 53, 69);
                        case RESCHEDULED -> new BaseColor(255, 140, 0);
                        default          -> BaseColor.DARK_GRAY;
                    });
            PdfPCell statusCell = new PdfPCell(new Phrase(i.getStatus().name(), statusFont));
            statusCell.setBackgroundColor(bg);
            statusCell.setPadding(7);
            statusCell.setBorderColor(new BaseColor(220, 220, 230));
            statusCell.setBorderWidth(0.4f);
            table.addCell(statusCell);
        }

        document.add(table);

        // ── Footer ───────────────────────────────────────────────────────────────
        document.add(Chunk.NEWLINE);
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 7, BaseColor.GRAY);
        Paragraph footer = new Paragraph("Confidential — Smart Interview Scheduler | Auto-generated report", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    private void addCell(PdfPTable table, String text, Font font, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(7);
        cell.setBorderColor(new BaseColor(220, 220, 230));
        cell.setBorderWidth(0.4f);
        table.addCell(cell);
    }
}
