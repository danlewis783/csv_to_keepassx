package com.danlewisracing.csvxml;

import org.csveed.annotations.CsvCell;
import org.csveed.annotations.CsvFile;
import org.csveed.bean.ColumnNameMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CsvFile(separator=',', mappingStrategy = ColumnNameMapper.class)
public class Entry implements Comparable<Entry> {
    @CsvCell(columnName = "name")
    private String title;

    @CsvCell(columnName = "username")
    private String userName;

    @CsvCell(columnName = "password")
    private String password;

    @CsvCell(columnName = "url")
    private String url;

    @CsvCell(columnName = "extra")
    private String comment;

    @Nullable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Nullable
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Nullable
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Entry() {
    }

    @Override
    public String toString() {
        return String.format("Entry{title='%s', userName='%s', password='%s', url='%s', comment='%s'}",
                title == null ? "" : title,
                userName == null ? "" : userName,
                password == null ? "" : password,
                url == null ? "" : url,
                comment == null ? "" : comment);
    }

    @Override
    public int compareTo(@NotNull Entry other) {
        if (title == null) {
            if (other.title == null) {
                return 0;
            } else {
                return 1;
            }
        }
        if (other.title == null) {
            return -1;
        }
        return title.toLowerCase().compareTo(other.title.toLowerCase());
    }
}
