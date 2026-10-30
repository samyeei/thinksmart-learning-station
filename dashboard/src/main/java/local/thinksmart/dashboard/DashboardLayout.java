package local.thinksmart.dashboard;
/** Design coordinates keep portrait cards large and fully on screen. */
final class DashboardLayout {
 final int width,height,quoteY;final float[][]cards;
 DashboardLayout(boolean portrait){width=portrait?800:1280;height=portrait?1280:800;quoteY=portrait?1160:704;cards=portrait?new float[][]{{36,300,728,430},{36,750,478,380},{530,750,234,184},{530,950,234,180}}:new float[][]{{36,252,650,417},{696,252,326,417},{1034,252,210,211},{1034,475,210,194}};}
}
