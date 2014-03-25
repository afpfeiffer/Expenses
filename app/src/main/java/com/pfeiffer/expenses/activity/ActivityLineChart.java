package com.pfeiffer.expenses.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import com.pfeiffer.expenses.R;
import com.pfeiffer.expenses.repository.RepositoryManager;
import com.pfeiffer.expenses.utility.Statistics;

public class ActivityLineChart extends Activity {

    private Statistics statistics_;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_chart);

        RepositoryManager repositoryManager = new RepositoryManager(this);
        repositoryManager.open();
        statistics_ = new Statistics(repositoryManager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_line_chart, menu);
        return true;
    }
}
