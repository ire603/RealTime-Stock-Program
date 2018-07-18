
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import martin.Company;
import martin.me.LiveStockQuoteService;
import pl.zankowski.iextrading4j.api.stocks.Quote;
import pl.zankowski.iextrading4j.client.IEXTradingClient;
import pl.zankowski.iextrading4j.client.rest.request.stocks.QuoteRequestBuilder;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class StockCanvas {
	private static final int MAX_DATA_POINTS_LINE = 20;
	private static final int MAX_DATA_POINTS_BAR = 20;
	private static ExecutorService executor;

	private static Series<String, Number> series1;
	private static Series<String, Number> series2;
	private static Series<String, Number> series3;
	private static String symbol;
	private static ConcurrentLinkedQueue<Number> queue1 = new ConcurrentLinkedQueue<>();
	private static ConcurrentLinkedQueue<Number> queue2 = new ConcurrentLinkedQueue<>();
	private static ConcurrentLinkedQueue<Number> queue3 = new ConcurrentLinkedQueue<>();
	private static LineChart<String, Number> lineChart;
	private static BarChart<String, Number> barChart;
    private static BarChart<String, Number> barChart1;
	private static final ObservableList<Company> listCompany = FXCollections.observableArrayList();

	private static CategoryAxis x;
    private static CategoryAxis xxx;


    public static Tab multiChart() {
		symbol = " ";
		NumberAxis y = new NumberAxis();
		NumberAxis yy = new NumberAxis();
        NumberAxis yyy = new NumberAxis();
		x = new CategoryAxis();

		x.setAutoRanging(true);
		x.setTickMarkVisible(true);

		y.setForceZeroInRange(false);
		y.setAutoRanging(true);
		y.setTickUnit(.10);

		CategoryAxis xx = new CategoryAxis();

		xx.setAutoRanging(true);
		xx.setTickMarkVisible(true);

		yy.setForceZeroInRange(false);
		yy.setAutoRanging(false);
		yy.setTickUnit(1000);
		listCompany.addAll(Company.getListCompanies());
		ComboBox<Company> companyComboBox = new ComboBox<Company>();
		companyComboBox.setItems(listCompany);

		lineChart = new LineChart<String, Number>(x,y) {
			protected void addStockItem(Series<String, Number> series, int itemIndex, Data<String, Number> item) {
			}
		};

		barChart = new BarChart<String, Number>(xx, yy) {
			protected void addStockItem(Series<String, Number> series, int itemIndex, Data<String, Number> item) {
			}
		};

		lineChart.setPrefSize(1400, 500);
		barChart.setPrefSize(1400, 200);

		lineChart.setAnimated(false);
		barChart.setAnimated(true);
		series1 = new Series<>();
		series2 = new Series<>();

		barChart.getYAxis().setAutoRanging(true);
		barChart.getXAxis().setAutoRanging(true);
		barChart.setLegendVisible(false);
		barChart.setHorizontalGridLinesVisible(true);

		lineChart.getXAxis().setAutoRanging(true);
		lineChart.getYAxis().setAutoRanging(true);
		lineChart.setTitle("Real Time: Last Price");
		lineChart.setCreateSymbols(false);
		lineChart.setLegendVisible(false);
		lineChart.setHorizontalGridLinesVisible(true);
		lineChart.setAxisSortingPolicy(
			   LineChart.SortingPolicy.NONE);
        TextField textField = new TextField();
        Button button = new Button("Start");
        Button button1 = new Button("Cancel");
        button1.setOnAction(event -> {
            start(false);
        });
        button.setOnAction(e -> {
            if (textField.getText() != null) {
                symbol = textField.getText();
                start(true);
            }
        });

		lineChart.getData().addAll(series1);
		barChart.getData().addAll(series2);

		ScrollPane scrollPane = new ScrollPane();

		Group group1 = new Group();
		group1.getChildren().add(scrollPane);

		GridPane gridPane = new GridPane();
		gridPane.add(lineChart, 0, 0);
		gridPane.add(barChart, 0, 1);

		scrollPane.setContent(gridPane);
		
		Tab tab = new Tab();
		
		HBox hbox = new HBox();
		hbox.getChildren().add(0, textField);
		hbox.getChildren().add(1, button);
		hbox.getChildren().add(2, button1);
		hbox.getChildren().add(3,  gridPane);
		
		BorderPane borderPane = new BorderPane();
		borderPane.setTop(hbox);
		borderPane.setCenter(scrollPane);

		tab.setContent(borderPane);

		return tab;
	}

	private static void start(boolean trueorfalse) {
        if (!trueorfalse) {
            System.out.printf("%nNo Sock Received");

        } else {
            executor = Executors.newCachedThreadPool(r ->  {

                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;

            });

            AddToQueue add = new AddToQueue();
            executor.execute(add);
            prepareTimeline();
        }


    }
	
    private static class AddToQueue implements Runnable {

        private IEXTradingClient iexTradingClient;


        @Override
		public void run() {
			try {
			    new LiveStockQuoteService(symbol);
                iexTradingClient = IEXTradingClient.create();
                Quote quoteService = iexTradingClient.executeRequest(new QuoteRequestBuilder()
                        .withSymbol(symbol)
                        .build());
                BigDecimal last = quoteService.getLatestPrice();
				BigDecimal volume = quoteService.getLatestVolume();

				queue1.add(last);
				queue2.add(volume);

				Thread.sleep(1000);
				executor.execute(this);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static void prepareTimeline() {
		new AnimationTimer() {
			@Override
			public void handle(long now) {
				addDataToQueue();
			}
		}.start();
	}

	private static void addDataToQueue() {
		int ii = 0;
		for (int i = 0; i < 1000; i++) {
			
			if (queue1.isEmpty()) {
				break;
			}
			
			Date date = new Date();
			String date1 = String.format(
					"%s:%s:%s%n%.2f%n%.1f",
					date.getHours(),
					date.getMinutes(),
					date.getSeconds(),
					queue1.element().doubleValue(),
					queue2.element().doubleValue());


			series1.getData().add(new Data<>(date1,
                    queue1.remove()
                            .doubleValue()));

			series2.getData().add(new Data<>(
                    date1,
                    queue2.remove().doubleValue()));
	
		}
	
		if (series1.getData().size() > MAX_DATA_POINTS_LINE) {
			series1.getData().remove(ii,
					series1.getData().size()
						   - MAX_DATA_POINTS_LINE);
		}
	
		if (series2.getData().size() > MAX_DATA_POINTS_BAR) {
			series2.getData().remove(ii,
					series2.getData().size()
						   - MAX_DATA_POINTS_BAR);
		}

	}

}
