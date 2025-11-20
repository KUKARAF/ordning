# Change: Add PDF Ticket Processing Core

## Why
Enable the app to read and parse PDF tickets to extract travel information, QR codes, and timing details for calendar event creation.

## What Changes
- Add PDF processing capability using PdfiumAndroid
- Implement QR code detection and extraction
- Create ticket data model with travel details
- Add local storage for processed tickets
- **BREAKING**: Requires new file permissions and storage model

## Impact
- Affected specs: pdf-processing (new)
- Affected code: Core data layer, file system access, UI components