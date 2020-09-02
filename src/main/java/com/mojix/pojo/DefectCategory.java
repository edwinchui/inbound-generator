package com.mojix.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DefectCategory {

    private List<String> hierarchyNames;
    private String type;
    private List<String> parentLevelName;
    private String name;
    private String code;

}
