package com.unimelb.cis.structures.queryadaptive;

import com.unimelb.cis.CSVFileReader;
import com.unimelb.cis.CSVFileWriter;
import com.unimelb.cis.geometry.Mbr;

import java.util.ArrayList;
import java.util.List;

public class QueryProfile {

    public static void main(String args[]) {
        genQueryProfiles();
    }

    public static void genQueryProfiles() {
        List<Mbr> mbrs = Mbr.getMbrs(0.01f,100,2);
        CSVFileWriter writer = new CSVFileWriter();
//        writer.writeQueryProfile(mbrs, "..\\dataset\\" + "QueryProfiles_0.01%_" + ".csv");
    }

    public static List<Mbr> getQueryProfile(String f1) {
        CSVFileReader reader = new CSVFileReader();
        List<String> lines = reader.read(f1);

        List<Mbr> mbrs = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            mbrs.add(Mbr.genMbr(lines.get(i), ","));
        }
        return mbrs;
    }

}
