package com.fexed.coffeecounter.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.PieRenderer;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;
import com.fexed.coffeecounter.R;
import com.fexed.coffeecounter.data.Coffeetype;
import com.fexed.coffeecounter.data.Cup;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
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
import java.util.concurrent.ExecutionException;

/**
 * {@code Fragment} with graphs and stats
 * Created by Federico Matteoni on 22/06/2020
 */
public class StatFragment extends Fragment implements View.OnClickListener {
    private GraphView graph;
    private GraphView daygraph;
    private PieChart pie;
    private TextView totalcupstxtv;
    private TextView totalcupslastmonthtxtv;
    private TextView totalliterstxtv;

    public static StatFragment newInstance() {
        return new StatFragment();
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
        adInitializer();
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        ImageButton historyinfobtn = getView().findViewById(R.id.historyinfotbn);
        ImageButton daysinfobtn = getView().findViewById(R.id.daysinfobtn);
        ImageButton typesinfobtn = getView().findViewById(R.id.typesinfobtn);
        historyinfobtn.setOnClickListener(this);
        daysinfobtn.setOnClickListener(this);
        typesinfobtn.setOnClickListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        ImageButton historyinfobtn = getView().findViewById(R.id.historyinfotbn);
        ImageButton daysinfobtn = getView().findViewById(R.id.daysinfobtn);
        ImageButton typesinfobtn = getView().findViewById(R.id.typesinfobtn);
        historyinfobtn.setOnClickListener(null);
        daysinfobtn.setOnClickListener(null);
        typesinfobtn.setOnClickListener(null);
    }

    /**
     * Initializes the ads
     */
    public void adInitializer() {
        MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                AdView mAdView2 = getView().findViewById(R.id.banner2);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView2.loadAd(adRequest);
                AdView mAdView3 = getView().findViewById(R.id.banner3);
                adRequest = new AdRequest.Builder().build();
                mAdView3.loadAd(adRequest);
            }
        });
    }

    /**
     * Initialiazes the history graph
     * @param graph the history graph
     */
    public void historyGraphInitializer(GraphView graph) {
        graph.getViewport().setMaxXAxisSize(30);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
    }

    /**
     * Initialiazes the day graph
     * @param daygraph the day graph
     */
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

    /**
     * Initializes the graphs
     */
    public void graphInitializer() {
        historyGraphInitializer(graph);
        daygraphInitializer(daygraph);
    }

    /**
     * Returns a {@code Date} from a {@code yyyy/MM/dd} string
     * @param date the {@code String} to be parsed
     * @return the parsed {@code Date}, or {@code null} if and incorrect {@code String} was given
     */
    public Date getLocalDateFromString(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        try {
            return format.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Returns a {@code yyyy/MM/dd} string from a {@code Date}
     * @param date the {@code Date} to be formatted
     * @return the formatted {@code String}
     */
    public String getStringFromLocalDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        return format.format(date);
    }

    /**
     * Adds {@code toadd} days to a {@code Date}
     * @param from the starting {@code Date}
     * @param toadd the number of days to be added
     * @return the new {@code Date}
     */
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

    /**
     * Fills the history graph
     * @param graph the history graph
     */
    public void historyGraph(final GraphView graph) {
        //days[0] is always the first day in the db
        //days.length = cups.length
        AsyncTask<Void, Void, List<String>> daystask = MainActivity.db.getDays();
        AsyncTask<Void, Void, List<Integer>> cupsTask = MainActivity.db.perDay();

        try {
            final List<String> days = daystask.get();
            final List<Integer> cups = cupsTask.get();
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
        } catch (InterruptedException | ExecutionException ignored) {}
    }


    /**
     * Fills the pie graph
     * @param pie the pie graph
     */
    @SuppressLint("ClickableViewAccessibility")
    public void typePie(final PieChart pie) {
        pie.clear();
        try {
            List<Coffeetype> types = MainActivity.db.getTypes().get();
            int totalcups = 0;
            for (Coffeetype type : types) totalcups += type.getQnt();
            for (Coffeetype type : types) {
                int clr;
                if (type.isFav()) {
                    clr = getResources().getColor(R.color.colorAccent);
                } else clr = getResources().getColor(R.color.colorAccentDark);

                String name = "";
                int n = type.getQnt();
                double perc;
                if (totalcups > 0) perc = (double) (n * 100) / totalcups;
                else perc = 0;
                if (perc > 2.5) name = type.getName();
                Segment segment = new Segment(name, n);
                SegmentFormatter formatter = new SegmentFormatter(clr);
                formatter.setRadialInset((float) 1);
                Paint pnt = new Paint(formatter.getLabelPaint());
                pnt.setTextSize(30);
                if (type.isFav()) {
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
        } catch (Exception ignored) {}

        pie.redraw();
    }

    /**
     * Gets the localized name of the {@code n+1} day of the week
     *
     * Could be probably done way better
     * @param n the day of the week
     * @return the localized name
     */
    public String dayFromNumber(int n) {
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

    /**
     * Fills the day graph
     * @param daygraph the day graph
     */
    public void dayGraph(final GraphView daygraph) {
        try {
            List<Cup> allcups = new ArrayList<>();
            List<Coffeetype> coffeetypes = MainActivity.db.getTypes().get();
            for (Coffeetype type : coffeetypes) {
                allcups.addAll(MainActivity.db.getCups(type.getKey()).get());
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
        } catch (InterruptedException | ExecutionException ignored) {}
    }

    /**
     * Updates graphs and stats
     */
    public void graphUpdater() {
        Calendar c = Calendar.getInstance();
        int curmonth = c.get(Calendar.MONTH);
        int curyear = c.get(Calendar.YEAR);
        int month;
        int year;
        int cupstotal = 0;
        int cupstotal_lastmonth = 0;
        int milliliterstotal = 0;

        try {
            for (Coffeetype type : MainActivity.db.getTypes().get()) {
                if (type.isLiquido()) milliliterstotal += (type.getLiters() * type.getQnt());
                cupstotal += type.getQnt();
            }
            for (String day : MainActivity.db.getDays().get()) {
                Date date = getLocalDateFromString(day);
                c.setTime(date);
                month = c.get(Calendar.MONTH);
                year = c.get(Calendar.YEAR);
                if (month == curmonth && year == curyear) {
                    cupstotal_lastmonth += MainActivity.db.getCups(day).get().size();
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
        } catch (InterruptedException | ExecutionException ignored) {}

        historyGraph(graph);
        typePie(pie);
        dayGraph(daygraph);
    }

    @Override
    public void onClick(View v) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.info));
        builder.setIcon(R.drawable.ic_info);
        switch (v.getId()) {
            case R.id.daysinfobtn:
                builder.setMessage(R.string.daysinfo);
                break;
            case R.id.historyinfotbn:
                builder.setMessage(R.string.historyinfo);
                break;
            case R.id.typesinfobtn:
                builder.setMessage(R.string.typesinfo);
                break;
        }

        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create();
        builder.show();
    }
}
