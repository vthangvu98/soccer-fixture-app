package com.thangv.SoccerFixturesApp.rapidapi;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TeamResponseList {

    private List<TeamItem> list;
}
