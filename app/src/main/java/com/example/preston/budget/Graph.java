package com.example.preston.budget;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.util.ArrayUtils;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Vector;

public class Graph extends Utils {

    GraphView the_graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph);

        // We expect to be given a string of space separated
        // floats e.g. "20.35 589.56 45.12 69.35 "
        String values = "";
        Bundle bundle = getIntent().getExtras();
        if(bundle == null)
        {
            Toast.makeText(this, "Error, Null Bundle", Toast.LENGTH_SHORT).show();
            return;
        }

        values = bundle.getString("values");
        if (values.isEmpty())
        {
            Toast.makeText(this, "Error, No Data to Graph", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, values, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] y_axis_data_strings = values.split(" ");
        if (y_axis_data_strings.length == 0)
        {
            Toast.makeText(this, "Error, Bad Graph Data", Toast.LENGTH_SHORT).show();
            return;
        }

        the_graph = (GraphView) findViewById(R.id.graph);
        the_graph.setVisibility(View.VISIBLE);
        Vector<DataPoint> data_point_vector = new Vector<DataPoint>();
        for(int i =0; i < y_axis_data_strings.length; i++)
        {
            try
            {
                data_point_vector.add(
                    new DataPoint(
                            i,
                            Double.parseDouble(y_axis_data_strings[i])
                    )
                );
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Error, Failed to Parse", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, y_axis_data_strings[i], Toast.LENGTH_SHORT).show();
                return;
            }
        }

        DataPoint[] data_points = new DataPoint[]{
            new DataPoint(0, data_point_vector.get(0).getY())
        };
        for(int i =1; i < data_point_vector.size(); i++)
        {
            data_points = ArrayUtils.appendToArray(data_points, new DataPoint(i, data_point_vector.get(i).getY()));
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(data_points);
        the_graph.addSeries(series);

    }
}
