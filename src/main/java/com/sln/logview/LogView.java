package com.sln.logview;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Duration;

public class LogView extends ListView<LogRecord> {
    private static final int MAX_ENTRIES = 1000;

    private final static PseudoClass debug = PseudoClass.getPseudoClass("debug");
    private final static PseudoClass info  = PseudoClass.getPseudoClass("info");
    private final static PseudoClass warn  = PseudoClass.getPseudoClass("warn");
    private final static PseudoClass error = PseudoClass.getPseudoClass("error");

    private final static SimpleDateFormat timestampFormatter = new SimpleDateFormat("HH:mm:ss");

    private final BooleanProperty       showTimestamp = new SimpleBooleanProperty(true);
    private final ObjectProperty<Level> filterLevel   = new SimpleObjectProperty<>(null);
    private final BooleanProperty       tail          = new SimpleBooleanProperty(false);
    private final BooleanProperty       paused        = new SimpleBooleanProperty(false);
    private final DoubleProperty        refreshRate   = new SimpleDoubleProperty(2);

    private final ObservableList<LogRecord> logItems = FXCollections.observableArrayList();
    
    private static void copyToClipboard(String str) {
    	final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(str);
        //content.putHtml("<b>Some</b> text");
        clipboard.setContent(content);
    }
    
    public BooleanProperty showTimeStampProperty() {
        return showTimestamp;
    }

    public ObjectProperty<Level> filterLevelProperty() {
        return filterLevel;
    }

    public BooleanProperty tailProperty() {
        return tail;
    }

    public BooleanProperty pausedProperty() {
        return paused;
    }

    public DoubleProperty refreshRateProperty() {
        return refreshRate;
    }
    
    public LogView(LogAppender logAppender) {
    	this(logAppender.getLogger());
    }

    public LogView(Logger logger) {
        getStyleClass().add("log-view");		// this should be styled with styles specified for .log-view class
        getStylesheets().add("/logview.css");	// read styles (for .log-view class) from .css file

        Timeline logTransfer = new Timeline(
                new KeyFrame(
                        Duration.seconds(1),
                        event -> {
                            logger.drainTo(logItems);

                            if (logItems.size() > MAX_ENTRIES) {
                                logItems.remove(0, logItems.size() - MAX_ENTRIES);
                            }

                            if (tail.get()) {
                                scrollTo(logItems.size());
                            }
                        }
                )
        );
        logTransfer.setCycleCount(Timeline.INDEFINITE);
        logTransfer.rateProperty().bind(refreshRateProperty());

        this.pausedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && logTransfer.getStatus() == Animation.Status.RUNNING) {
                logTransfer.pause();
            }

            if (!newValue && logTransfer.getStatus() == Animation.Status.PAUSED && getParent() != null) {
                logTransfer.play();
            }
        });

        this.parentProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                logTransfer.pause();
            } else {
                if (!paused.get()) {
                    logTransfer.play();
                }
            }
        });

        filterLevel.addListener((observable, oldValue, newValue) -> {
            setItems(
                    new FilteredList<LogRecord>(
                            logItems,
                            logRecord ->
                                logRecord.getLevel().ordinal() >=
                                filterLevel.get().ordinal()
                    )
            );
        });
        filterLevel.set(Level.DEBUG);
        
        // Add context menu: Copy selected, Copy all
        MenuItem copySelected = new MenuItem("Copy selected");
        MenuItem copyAll = new MenuItem("Copy all");
        copySelected.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	List<LogRecord> list = LogView.this.getSelectionModel().getSelectedItems();
            	String str = list.stream()
            		.map(LogRecord::toString)
            		.collect(Collectors.joining("\n"));
            	LogView.copyToClipboard(str);
            }
        });
        copyAll.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	List<LogRecord> list = LogView.this.getItems(); 
            	String str = list.stream()
            		.map(LogRecord::toString)
            		.collect(Collectors.joining("\n"));
            	LogView.copyToClipboard(str);
            }
        });
        ContextMenu listContextMenu = new ContextMenu();
        listContextMenu.getItems().addAll(copySelected, copyAll);
        this.setContextMenu(listContextMenu);
        // multiple lines can be selected
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Set cell factory of this ListView
        setCellFactory(param -> new ListCell<LogRecord>() {
            {
                showTimestamp.addListener(observable -> updateItem(this.getItem(), this.isEmpty()));
            }

            @Override
            protected void updateItem(LogRecord item, boolean empty) {
                super.updateItem(item, empty);

                pseudoClassStateChanged(debug, false);
                pseudoClassStateChanged(info, false);
                pseudoClassStateChanged(warn, false);
                pseudoClassStateChanged(error, false);

                if (item == null || empty) {
                    setText(null);
                    return;
                }

                String context = (item.getContext() == null) ? "" : item.getContext() + " ";

                if (showTimestamp.get()) {
                    String timestamp = (item.getTimestamp() == null) ? "" : timestampFormatter.format(item.getTimestamp()) + " ";
                    setText(timestamp + context + item.getMessage());
                } else {
                    setText(context + item.getMessage());
                }

                switch (item.getLevel()) {
                    case DEBUG:
                        pseudoClassStateChanged(debug, true);
                        break;

                    case INFO:
                        pseudoClassStateChanged(info, true);
                        break;

                    case WARN:
                        pseudoClassStateChanged(warn, true);
                        break;

                    case ERROR:
                        pseudoClassStateChanged(error, true);
                        break;
                }
            }
        });
    }
    
}
