package com.guzem.uzaktan.service.admin;

import java.io.IOException;

/**
 * Responsible only for packaging assignment submissions into a ZIP archive.
 * Extracted from AssignmentManagementService to keep that interface focused
 * on CRUD operations.
 */
public interface SubmissionZipService {

    byte[] downloadSubmissionsZip(Long assignmentId, Long requestingUserId) throws IOException;
}
