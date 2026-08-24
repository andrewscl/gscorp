export interface OperationsDashboardResponse {
  projectSiteShiftsSummary: ProjectSiteShiftRequestsSummaryDto[];
  projectSiteAttendancesSummary: ProjectSiteAttendancesSummaryDto[];
  projectSiteShiftsCoverageSummary: ProjectSiteShiftsSummaryDto[];
}

export interface ProjectSiteShiftRequestsSummaryDto {
    projectId: number;
    projectName: string;
    siteId: number;
    siteName: string;
    totalShiftsToday: number;
}

export interface ProjectSiteAttendancesSummaryDto {
    projectId: number; 
    projectName: string;
    siteId: number;
    siteName: string;
    attendanceCount: number;
}

export interface ProjectSiteShiftsSummaryDto {
    projectExternalId: string;
    projectName: string;
    siteExternalId: string;
    siteName: string;
    shifts: number;
    validShifts: number;
    coveredShifts: number;
    uncoveredShifts: number;
    coveredPercentage: number;
    uncoveredPercentage: number;
}







