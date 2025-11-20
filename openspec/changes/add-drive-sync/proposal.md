# Change: Add Google Drive Sync

## Why
Enable multi-user ticket synchronization via Google Drive folder "ordnung_ticketoza" with JSON tracking and MD5-based file organization for collaborative ticket management.

## What Changes
- Add Google Drive API integration
- Implement MD5-based file organization
- Create JSON tracking system for download status
- Add folder creation and management
- Implement bidirectional sync logic
- **BREAKING**: Requires Google Drive permissions and storage model changes

## Impact
- Affected specs: drive-sync (new)
- Affected code: Storage layer, sync services, file management, settings