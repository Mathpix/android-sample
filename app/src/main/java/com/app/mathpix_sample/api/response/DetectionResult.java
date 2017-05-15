package com.app.mathpix_sample.api.response;

import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by admin on 3/19/17.
 *
 *
 {
     "detection_list": [],
     "detection_map": {
         "contains_chart": 0,
         "contains_diagram": 0,
         "contains_geometry": 0,
         "contains_graph": 0,
         "contains_table": 0,
         "is_inverted": 0,
         "is_not_math": 0,
         "is_printed": 0
     },
     "error": "",
     "latex": "\\lim _ { x \\rightarrow 3} ( \\frac { x ^ { 2} + 9} { x - 3} )",
     "latex_confidence": 0.86757309488734,
     "position": {
         "height": 273,
         "top_left_x": 57,
         "top_left_y": 14,
         "width": 605
     }
 }
 */

public class DetectionResult {
    public DetectionMap detection_map;
    public String error;
    public String latex;
    public ArrayList<String> latex_list;
    public double latex_confidence;
    public Position position;

    public static class DetectionMap {
        public double contains_chat;
        public double contains_diagram;
        public double contains_geometry;
        public double contains_graph;
        public double contains_table;

        public double is_inverted;
        public double is_not_math;
        public double is_printed;
    }

    public static class Position {
        public double width;
        public double height;
        public double top_left_x;
        public double top_left_y;
    }
}
