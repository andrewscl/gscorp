export interface OperationsDashboardResponse {
    projectSiteShiftRequestsSummary: ProjectSiteShiftRequestsSummaryDto[];
    projectSiteAttendancesSummary: ProjectSiteAttendancesSummaryDto[];
    projectSiteShiftsSummary: ProjectSiteShiftsSummaryDto[];
    shiftsCountLast24Hours: ShiftsCountLast24HoursDto[];
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
export interface ShiftsCountLast24HoursDto {
    totalShifts: number;
    unplannedShifts: number;
    plannedShifts: number;
    inProgressShifts: number;
    completedShifts: number;
    cancelledShifts: number;
    pendingShifts: number;
    uncoveredShifts: number;
    startTs: string;
}
