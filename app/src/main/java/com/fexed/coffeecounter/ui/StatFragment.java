package com.fexed.coffeecounter.ui;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.PieRenderer;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;
import com.fexed.coffeecounter.R;
import com.fexed.coffeecounter.data.Coffeetype;
import com.fexed.coffeecounter.data.Cup;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.OnBalloonOutsideTouchListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Federico Matteoni on 22/06/2020
 */
public class StatFragment extends Fragment {
    private GraphView graph;
    private GraphView daygraph;
    private PieChart pie;
    private TextView totalcupstxtv;
    private TextView totalcupslastmonthtxtv;
    private TextView totalliterstxtv;

    public static StatFragment newInstance(int index) {
        StatFragment fragment = new StatFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstancestate) {
        super.onCreate(savedInstancestate);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstancestate) {
        View root = inflater.inflate(R.layout.activity_stat, container, false);
        graph = root.findViewById(R.id.historygraph);
        daygraph = root.findViewById(R.id.daygraph);
        pie = root.findViewById(R.id.piegraph);
        totalcupstxtv = root.findViewById(R.id.totalcups);
        totalcupslastmonthtxtv = root.findViewById(R.id.totalcups_lastmonth);
        totalliterstxtv = root.findViewById(R.id.totalliters);
        graphInitializer();
        graphUpdater();
        return root;
    }

    public void historyGraphInitializer(GraphView graph) {
        graph.getViewport().setMaxXAxisSize(30);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
    }

    public void daygraphInitializer(GraphView daygraph) {
        daygraph.getViewport().setMaxXAxisSize(7);
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(daygraph);
        staticLabelsFormatter.setHorizontalLabels(new String[]{dayFromNumber(0),
                dayFromNumber(1),
                dayFromNumber(2),
                dayFromNumber(3),
                dayFromNumber(4),
                dayFromNumber(5),
                dayFromNumber(6)});
        daygraph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        daygraph.getViewport().setYAxisBoundsManual(true);
        daygraph.getViewport().setMinY(0);
    }

    public void graphInitializer() {
        historyGraphInitializer(graph);
        daygraphInitializer(daygraph);
    }

    public Date getLocalDateFromString(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        try {
            return format.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public String getStringFromLocalDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        return format.format(date);
    }

    public Date plusDays(Date from, int toadd) {
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(from);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        c.add(Calendar.DAY_OF_MONTH, toadd);
        return c.getTime();
    }

    public void historyGraph(final GraphView graph) {
        //days[0] è sempre il primo giorno nel MainActivity.db
        //days.length = cups.length
        final List<String> days = MainActivity.db.cupDAO().getDays();
        final List<Integer> cups = MainActivity.db.cupDAO().perDay();

        if (days.size() > 0) {
            Date fromDate = getLocalDateFromString(days.get(0));
            Date toDate = Calendar.getInstance().getTime();
            Date current = fromDate;
            toDate = plusDays(toDate, 1);
            List<Date> dates = new ArrayList<>(25);
            while (current.getTime() < toDate.getTime()) {
                dates.add(current);
                current = plusDays(current, 1);
            }

            graph.getViewport().setScalable(true);
            //graph.getViewport().setMinY(0);
            //graph.getViewport().setMaxY(20);
            graph.getViewport().setMaxX((dates.get(dates.size() - 2)).getTime());
            if (dates.size() <= 30)
                graph.getViewport().setMinX((dates.get(0)).getTime());
            else
                graph.getViewport().setMinX((dates.get(dates.size() - 29)).getTime());
            graph.getViewport().setScalable(false);

            List<DataPoint> points = new ArrayList<>();
            int j = 0;
            int i, maxc = 0;
            for (i = 0; i < dates.size(); i++) {
                String day = getStringFromLocalDate(dates.get(i));
                Date daydate = dates.get(i);
                if (j < days.size() && day.equals(days.get(j))) {
                    points.add(new DataPoint(daydate, cups.get(j)));
                    if (cups.get(j) > maxc) maxc = cups.get(j);
                    j++;
                } else points.add(new DataPoint(daydate, 0));
            }
            DataPoint[] pointsv = new DataPoint[points.size()];
            pointsv = points.toArray(pointsv);
            graph.removeAllSeries();
            graph.getViewport().setMaxY(maxc);

            if (MainActivity.state.getBoolean("historyline", false)) {
                BarGraphSeries<DataPoint> seriesb = new BarGraphSeries<>(pointsv);
                seriesb.setDrawValuesOnTop(true);
                seriesb.setColor(getResources().getColor(R.color.colorAccent));
                seriesb.setSpacing(25);
                graph.addSeries(seriesb);
                seriesb.setOnDataPointTapListener(new OnDataPointTapListener() {
                    @Override
                    public void onTap(Series series, DataPointInterface dataPoint) {
                        DateFormat mDateFormat = android.text.format.DateFormat.getDateFormat(getContext());
                        Calendar mCalendar = Calendar.getInstance();
                        mCalendar.setTimeInMillis((long) dataPoint.getX());
                        final Balloon balloon = new Balloon.Builder(getContext())
                                .setText(mDateFormat.format(mCalendar.getTimeInMillis()) + ": " + (int) dataPoint.getY() + " " + getString(R.string.tazzine_totali).toLowerCase())
                                .setWidthRatio(0.5f)
                                .setBackgroundColorResource(R.color.colorAccent)
                                .setBalloonAnimation(BalloonAnimation.FADE)
                                .setArrowVisible(false)
                                .build();
                        balloon.setOnBalloonOutsideTouchListener(new OnBalloonOutsideTouchListener() {
                            @Override
                            public void onBalloonOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                                balloon.dismiss();
                            }
                        });
                        balloon.showAlignBottom(graph);
                    }
                });
            } else {
                LineGraphSeries<DataPoint> seriesl = new LineGraphSeries<>(pointsv);
                seriesl.setColor(getResources().getColor(R.color.colorAccent));
                graph.addSeries(seriesl);
                seriesl.setOnDataPointTapListener(new OnDataPointTapListener() {
                    @Override
                    public void onTap(Series series, DataPointInterface dataPoint) {
                        DateFormat mDateFormat = android.text.format.DateFormat.getDateFormat(getContext());
                        Calendar mCalendar = Calendar.getInstance();
                        mCalendar.setTimeInMillis((long) dataPoint.getX());
                        final Balloon balloon = new Balloon.Builder(getContext())
                                .setText(mDateFormat.format(mCalendar.getTimeInMillis()) + ": " + (int) dataPoint.getY() + " " + getString(R.string.tazzine_totali).toLowerCase())
                                .setWidthRatio(0.5f)
                                .setBackgroundColorResource(R.color.colorAccent)
                                .setBalloonAnimation(BalloonAnimation.FADE)
                                .setArrowVisible(false)
                                .build();
                        balloon.setOnBalloonOutsideTouchListener(new OnBalloonOutsideTouchListener() {
                            @Override
                            public void onBalloonOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                                balloon.dismiss();
                            }
                        });
                        balloon.showAlignBottom(graph);
                    }
                });
                seriesl.setThickness(5);
                seriesl.setDataPointsRadius(10);
                seriesl.setDrawDataPoints(true);
            }

            // set date label formatter
            graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getContext()) {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    SimpleDateFormat format = new SimpleDateFormat("dd/MM", Locale.getDefault());
                    if (isValueX) {
                        // format as date
                        mCalendar.setTimeInMillis((long) value);
                        return format.format(mCalendar.getTimeInMillis());
                    } else {
                        return super.formatLabel(value, false);
                    }
                }
            });
            graph.getGridLabelRenderer().setHumanRounding(false);
            graph.getViewport().setXAxisBoundsManual(true);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void typePie(final PieChart pie) {
        pie.clear();
        List<Coffeetype> types = MainActivity.db.coffetypeDao().getAll();
        List<Coffeetype> favs = MainActivity.db.coffetypeDao().getFavs();
        int totalcups = 0;
        for (Coffeetype type : types) totalcups += type.getQnt();
        for (Coffeetype type : types) {
            int clr;
            boolean isfav = favs.contains(type);
            if (isfav) {
                clr = getResources().getColor(R.color.colorAccent);
            } else clr = getResources().getColor(R.color.colorAccentDark);

            String name = "";
            int n = MainActivity.db.cupDAO().getAll(type.getKey()).size();
            double perc;
            if (totalcups > 0) perc = (double) (n * 100) / totalcups;
            else perc = 0;
            if (perc > 2.5) name = type.getName();
            Segment segment = new Segment(name, n);
            SegmentFormatter formatter = new SegmentFormatter(clr);
            formatter.setRadialInset((float) 1);
            Paint pnt = new Paint(formatter.getLabelPaint());
            pnt.setTextSize(30);
            if (MainActivity.db.coffetypeDao().getFavs().contains(type)) {
                pnt.setFakeBoldText(true);
            }
            formatter.setLabelPaint(pnt);
            pie.addSegment(segment, formatter);
        }

        pie.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                view.performClick();
                PointF click = new PointF(motionEvent.getX(), motionEvent.getY());
                if (pie.getPie().containsPoint(click)) {
                    Segment segment = pie.getRenderer(PieRenderer.class).getContainingSegment(click);

                    if (segment != null) {
                        int n = segment.getValue().intValue();
                        String str = segment.getTitle();

                        final Balloon balloon = new Balloon.Builder(getContext())
                                .setText(str + ": " + n)
                                .setWidthRatio(0.5f)
                                .setBackgroundColorResource(R.color.colorAccent)
                                .setBalloonAnimation(BalloonAnimation.FADE)
                                .setArrowVisible(false)
                                .build();
                        balloon.setOnBalloonOutsideTouchListener(new OnBalloonOutsideTouchListener() {
                            @Override
                            public void onBalloonOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                                balloon.dismiss();
                            }
                        });
                        balloon.showAlignBottom(pie);
                    }
                }
                return true;
            }
        });

        pie.redraw();
    }

    public String dayFromNumber(int n) { //Get localized day name
        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("E", Locale.getDefault());
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyy/MM/dd", Locale.getDefault());

        try {
            switch (n) {
                case 0:
                    date = sdf1.parse("2019/09/15");
                    break;
                case 1:
                    date = sdf1.parse("2019/09/16");
                    break;
                case 2:
                    date = sdf1.parse("2019/09/17");
                    break;
                case 3:
                    date = sdf1.parse("2019/09/18");
                    break;
                case 4:
                    date = sdf1.parse("2019/09/19");
                    break;
                case 5:
                    date = sdf1.parse("2019/09/20");
                    break;
                case 6:
                    date = sdf1.parse("2019/09/21");
                    break;
            }

            return sdf.format(date);
        } catch (ParseException e) {
            return "";
        }

    }

    public void dayGraph(final GraphView daygraph) {
        List<Cup> allcups = new ArrayList<>();
        for (Coffeetype type : MainActivity.db.coffetypeDao().getAll()) {
            allcups.addAll(MainActivity.db.cupDAO().getAll(type.getKey()));
        }
        int[] cupPerDay = new int[7];
        SimpleDateFormat sdf = new SimpleDateFormat("E", Locale.getDefault());
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyy/MM/dd", Locale.getDefault());
        Calendar clndr = Calendar.getInstance();
        int day;
        for (Cup cup : allcups) {
            try {
                //Log.d("DAYS", sdf.format(sdf1.parse(cup.getDay())));
                clndr.setTime(sdf1.parse(cup.getDay()));
                day = clndr.get(Calendar.DAY_OF_WEEK) - 1;
                cupPerDay[day]++;
            } catch (ParseException ignored) {
            }
        }
        int max = 0;

        DataPoint[] pointsv = new DataPoint[7];
        for (int i = 0; i < 7; i++) {
            pointsv[i] = new DataPoint(i, cupPerDay[i]);
            if (cupPerDay[i] > max) max = cupPerDay[i];
        }

        BarGraphSeries<DataPoint> dayseries = new BarGraphSeries<>(pointsv);
        dayseries.setDrawValuesOnTop(true);
        dayseries.setColor(getResources().getColor(R.color.colorAccent));
        dayseries.setSpacing(25);

        dayseries.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                String day = dayFromNumber((int) dataPoint.getX());
                final Balloon balloon = new Balloon.Builder(getContext())
                        .setText(day + ": " + String.format(Locale.getDefault(), "%d", (int) dataPoint.getY()) + " " + getString(R.string.tazzine_totali).toLowerCase())
                        .setWidthRatio(0.5f)
                        .setBackgroundColorResource(R.color.colorAccent)
                        .setBalloonAnimation(BalloonAnimation.FADE)
                        .setArrowVisible(false)
                        .build();
                balloon.setOnBalloonOutsideTouchListener(new OnBalloonOutsideTouchListener() {
                    @Override
                    public void onBalloonOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                        balloon.dismiss();
                    }
                });
                balloon.showAlignBottom(daygraph);
            }
        });

        daygraph.removeAllSeries();
        daygraph.addSeries(dayseries);
        daygraph.getViewport().setMaxY(max);
    }

    public void graphUpdater() {
        Calendar c = Calendar.getInstance();
        int curmonth = c.get(Calendar.MONTH);
        int curyear = c.get(Calendar.YEAR);
        int month;
        int year;
        int cupstotal = 0;
        int cupstotal_lastmonth = 0;
        int milliliterstotal = 0;

        for (Coffeetype type : MainActivity.db.coffetypeDao().getAll()) {
            if (type.isLiquido()) milliliterstotal += (type.getLiters() * type.getQnt());
            cupstotal += type.getQnt();
        }
        for (String day : MainActivity.db.cupDAO().getDays()) {
            Date date = getLocalDateFromString(day);
            c.setTime(date);
            month = c.get(Calendar.MONTH);
            year = c.get(Calendar.YEAR);
            if (month == curmonth && year == curyear) {
                cupstotal_lastmonth += MainActivity.db.cupDAO().getAll(day).size();
            }
        }
        String str = "" + cupstotal;
        totalcupstxtv.setText(str);
        str = "" + cupstotal_lastmonth;
        totalcupslastmonthtxtv.setText(str);
        if (milliliterstotal < 1000) {
            str = milliliterstotal + " ml";
            totalliterstxtv.setText(str);
        } else {
            str = milliliterstotal / 1000 + " l";
            totalliterstxtv.setText(str);
        }

        historyGraph(graph);
        typePie(pie);
        dayGraph(daygraph);
    }
}
