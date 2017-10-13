package com.androstock.screenrecorder;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

/**
 * Created by advanz101 on 13/10/17.
 */

public class RecordedVideoModel extends SugarRecord{

    @SerializedName("video_lint")
    @Expose
    private String videoLink;
    @SerializedName("video_link_sd_card")
    @Expose
    private String videoLinkSdCard;
    @SerializedName("video_comment")
    @Expose
    private String videoComment;
    @SerializedName("video_date")
    @Expose
    private String videoDate;

    public String getVideoLink() {
        return videoLink;
    }

    public void setVideoLink(String videoLink) {
        this.videoLink = videoLink;
    }

    public String getVideoLinkSdCard() {
        return videoLinkSdCard;
    }

    public void setVideoLinkSdCard(String videoLinkSdCard) {
        this.videoLinkSdCard = videoLinkSdCard;
    }

    public String getVideoComment() {
        return videoComment;
    }

    public void setVideoComment(String videoComment) {
        this.videoComment = videoComment;
    }

    public String getVideoDate() {
        return videoDate;
    }

    public void setVideoDate(String videoDate) {
        this.videoDate = videoDate;
    }
}
