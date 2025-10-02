package com.thangv.SoccerFixturesApp.rapidapi;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamItem {
    private String name;
    private String shortName;
    private int id;
    private int played;
    private int wins;
    private int draws;
    private int losses;
    private String scoresStr;
    private int goalConDiff;
    private int pts;
    private int idx;
    private String qualColor;
    private String logo;
}