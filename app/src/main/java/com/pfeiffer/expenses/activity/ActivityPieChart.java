package com.pfeiffer.expenses.activity;

import android.app.Activity;


public class ActivityPieChart extends Activity {
//
//    // TODO better colors
//    private static int[] colors_ = new int[]{Color.GREEN, Color.BLUE, Color.MAGENTA, Color.CYAN};
//    private DataAnalysis statistics_;
//    private CategorySeries mSeries_ = new CategorySeries("");
//
//    private DefaultRenderer mRenderer_ = new DefaultRenderer();
//
//    private ArrayList<CATEGORY> usedCategoryList_ = new ArrayList<CATEGORY>();
//
//    private GraphicalView mChartView_;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_pie_chart);
//
//        RepositoryManager repositoryManager = new RepositoryManager(this);
//        repositoryManager.open();
//        statistics_ = new DataAnalysis(repositoryManager);
//
//        configureRenderer();
//
//        configurePieChart();
//
//
//        if (mChartView_ != null) {
//            mChartView_.repaint();
//        }
//
//    }
//
//    private void configureRenderer() {
//        mRenderer_.setApplyBackgroundColor(true);
//        mRenderer_.setBackgroundColor(Color.argb(100, 50, 50, 50));
//        mRenderer_.setLegendTextSize(40);
////        mRenderer_.setMargins(new int[]{20, 30, 15, 0});
//        mRenderer_.setZoomButtonsVisible(false);
//        mRenderer_.setStartAngle(90);
//        mRenderer_.setShowLegend(true);
//        mRenderer_.setShowLabels(false);
//    }
//
//    private void configurePieChart() {
//
//        String monthKey = statistics_.getCurrentYearMonthKey();
//        int counter = 0;
//
//        for (CATEGORY category : CATEGORY.values()) {
//            double expenses = statistics_.getExpensesForYearMonthAndCategory(monthKey, category);
//            if (expenses > 1e-3) {
//                usedCategoryList_.add(category);
//                String description = category.toString() + " " + Translation.getValidPrice(expenses) + " €";
//                mSeries_.add(description, expenses);
//                SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
//                renderer.setColor(colors_[counter % colors_.length]);
//                mRenderer_.addSeriesRenderer(renderer);
//                counter++;
//            }
//
//        }
//
//    }
//
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (mChartView_ == null) {
//            LinearLayout layout = (LinearLayout) findViewById(R.id.PieChart);
//            mChartView_ = ChartFactory.getPieChartView(this, mSeries_, mRenderer_);
//            mRenderer_.setClickEnabled(true);
//            mRenderer_.setSelectableBuffer(10);
//
//            mChartView_.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    SeriesSelection seriesSelection = mChartView_.getCurrentSeriesAndPoint();
//                    if (seriesSelection != null) {
//                        Toast.makeText(ActivityPieChart.this, usedCategoryList_.get(seriesSelection.getPointIndex())
//                                        + ": " + Translation.getValidPrice(seriesSelection.getValue()) + " €",
//                                Toast.LENGTH_SHORT
//                        ).show();
//                    }
//                }
//            });
//
//            mChartView_.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    return false;
//                }
//            });
//
//            layout.addView(mChartView_, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
//        } else {
//            mChartView_.repaint();
//        }
//    }
//
}