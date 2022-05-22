package com.example.TimeZones;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import elemental.json.JsonArray;
import org.springframework.scheduling.config.ScheduledTask;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SpringUI
@Title(value = "Время")
@Theme("mytheme")
@Push(value = PushMode.MANUAL, transport = Transport.WEBSOCKET)
public class MyUI extends UI {
    private Timer timer;
    private VerticalLayout verticalLayout;
    private Label header;
    private Label inform;
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        inform = new Label();
        inform.setContentMode(ContentMode.HTML);

        setupLayout(); // добавили вертикальный слой.
        addedHeader(); // добавляем заголовок.
        addButton();

    }

    private void setupLayout() {
        verticalLayout = new VerticalLayout();
        verticalLayout.setWidth(100, Unit.PERCENTAGE);
        verticalLayout.setHeight(50, Unit.PERCENTAGE);
        verticalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        verticalLayout.setSpacing(true);
        setContent(verticalLayout);
    }

    private void addedHeader() {
        header = new Label();
        header.setCaption("<h1>Часовые пояса</h1>");
        header.setCaptionAsHtml(true);
        header.setContentMode(ContentMode.HTML);
        HorizontalLayout horizontalLayout = new HorizontalLayout(header);
        horizontalLayout.setWidth(100, Unit.PERCENTAGE);
        horizontalLayout.setComponentAlignment(header, Alignment.MIDDLE_CENTER);

        verticalLayout.addComponent(horizontalLayout);
        verticalLayout.setComponentAlignment(horizontalLayout, Alignment.TOP_CENTER);
    }

    private void addButton() {
        Button givMeTimeZone = new Button("Получить локальное время !");
        Button givMeTimeZoneServer = new Button("Получить время сервера !");


        givMeTimeZoneServer.addClickListener(clickEvent -> {
            TimeZone timeZone = TimeZone.getTimeZone(ZoneId.systemDefault());
            inform.setValue("<h2>" + "Время сервера :" + dateTimeFormatter.format(LocalDateTime.now(ZoneId.systemDefault())) + " GMT ± " + (timeZone.getRawOffset() / 1000 / 3600) + timeZone.getID() + "</h2>");
        });

        givMeTimeZone.addClickListener(clickEvent -> {

            String givMeTimeZoneLastValue = givMeTimeZone.getCaption();
            givMeTimeZone.setCaption(!givMeTimeZone.getCaption().equals("Остановить !") ? "Остановить !" : "Получить время сервера !");

            if (givMeTimeZoneLastValue.equals("Остановить !")) {
                stopUpdateTimeUI();
                return;
            }
            startUpdateTimeUI();
        });

        JavaScript.getCurrent().addFunction("givMeTime", new JavaScriptFunction() {
            @Override
            public void call(JsonArray jsonArray) {
                String time = jsonArray.getString(0);
//                Notification.show("Время пользователя : " + time);
                inform.setValue("<h2>" + time + "</h2>");
            }
        });

        HorizontalLayout horizontalLayout = new HorizontalLayout(givMeTimeZone, givMeTimeZoneServer);
        horizontalLayout.setWidth(100, Unit.PERCENTAGE);
        horizontalLayout.setSpacing(true);
        horizontalLayout.setComponentAlignment(givMeTimeZone, Alignment.MIDDLE_CENTER);
        horizontalLayout.setComponentAlignment(givMeTimeZoneServer, Alignment.MIDDLE_CENTER);
        verticalLayout.addComponents(horizontalLayout, inform);
    }

    private void startUpdateTimeUI() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                getPushConfiguration().setPushMode(PushMode.MANUAL);
                getPushConfiguration().setTransport(Transport.WEBSOCKET);
                access(() -> {
                    int timeOffsetMil = Page.getCurrent().getWebBrowser().getTimezoneOffset() / 1000;
                    ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(timeOffsetMil);
                    LocalDateTime localDateTime = LocalDateTime.now(zoneOffset);
                    inform.setValue("<h2>" + "Часовой пояс : " + dateTimeFormatter.format(localDateTime) + "</h2>");
                    System.out.println("Обновляем UI ->" + "Часовой пояс : " + dateTimeFormatter.format(localDateTime));
                    push();
                });
            }
        };

        timer = new Timer("timerUpdateTimeUi");
        timer.scheduleAtFixedRate(timerTask, new Date(), 1000);

    }

    private void stopUpdateTimeUI() {
        System.out.println("Стоп обновление !");
        timer.cancel();
    }

}
