package com.example.server.domain.member.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum InterestedType {

    KOREA_TRIP("KOREA_TRIP", "국내여행"),
    OVERSEAS_TRIP("OVERSEAS_TRIP", "해외여행"),
    FREE("FREE", "자유"),
    COURSE("COURSE", "코스"),
    TOUR("TOUR", "투어"),
    SILENCE("SILENCE", "조용한"),
    COMFORT("COMFORT", "편안한"),
    FRIEND("FRIEND", "친구"),
    FAMILY("FAMILY", "가족"),
    ALONE("ALONE", "혼자"),
    HEALING("HEALING", "힐링"),
    ACTIVITY("ACTIVITY", "액티비티"),
    MONTH_LIVING("MONTH_LIVING", "한달살기"),
    NATURE("NATURE", "자연"),
    ATTRACTION("ATTRACTION", "명소"),
    FESTIVAL("FESTIVAL", "축제"),
    FOOD("FOOD", "먹거리"),
    HOTEL("HOTEL", "숙소"),
    BACKPACKING("BACKPACKING", "배낭여행"),
    CAR_CAMPING("CAR_CAMPING", "차박");




    private final String key;
    private final String title;

    InterestedType(String key, String title) {
        this.key = key;
        this.title = title;
    }

    public InterestedType fromName(String type) {
        return InterestedType.valueOf(type.toUpperCase());
    }

    public static InterestedType fromKoreanName(String title) {
        for (InterestedType value : values()) {
            if (value.getTitle().equals(title)) {
                return value;
            }
        }
        return null;
    }

    public static List<String> getAllInterestedType(){
        List<String> list = new ArrayList<>();
        for (InterestedType value : values()) {
            list.add(value.getTitle());
        }
        return list;
    }
}
