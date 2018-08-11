package com.edolfzoku.hayaemon2;

public class PlaylistItem {
    private String strNumber = null;
    private String strTitle = null;
    private String strArtist = null;

    public void setNumber(String strNumber) { this.strNumber = strNumber; }
    public String getNumber() { return strNumber; }
    public void setTitle(String strTitle) { this.strTitle = strTitle; }
    public String getTitle() { return strTitle; }
    public void setArtist(String strArtist) { this.strArtist = strArtist; }
    public String getArtist() { return strArtist; }

    public PlaylistItem() {};

    public PlaylistItem(String strNumber, String strTitle, String strArtist)
    {
        this.strNumber = strNumber;
        this.strTitle = strTitle;
        this.strArtist = strArtist;
    }
}
