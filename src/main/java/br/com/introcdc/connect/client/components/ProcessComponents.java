package br.com.introcdc.connect.client.components;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:07
 */

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProcessComponents {
    // Process Variables
    public static int LAST_ID = -1;
    public static Integer PROCESS = Integer.valueOf("0");
    public static Map<Integer, Process> PROCESS_MAP = new HashMap<>();
    public static Map<Integer, PrintWriter> WRITER_MAP = new HashMap<>();
    public static java.util.List<Integer> LOG_PROCESS = new ArrayList<>();
    public static Map<Integer, String> PROCESS_LIST = new HashMap<>();
}
