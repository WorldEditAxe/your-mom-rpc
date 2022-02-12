package me.q13x.yourMom;

public class CustomRPC {
    public String areaName;
    public String thumbnailText;
    public String thumbnailImage;

    CustomRPC(String line) {
        String[] splitString = line.split("\\|");
        if (splitString.length != 3) throw new Error("Invalid custom RPC entry! (entry: " + line + ")");

        this.areaName = splitString[0];
        this.thumbnailImage = splitString[1];
        this.thumbnailText = splitString[2];
    }
}
