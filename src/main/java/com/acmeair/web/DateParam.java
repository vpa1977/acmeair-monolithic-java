package com.acmeair.web;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class DateParam {
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy");
	private static int year = Calendar.getInstance().get(Calendar.YEAR);
	private Date date;

	public DateParam(String dateTime) {
		// A bit of hack to get the date in the correct format
		String dateOnly = dateTime.substring(0, 10) + " " + year;

		LocalDate localDate = LocalDate.parse(dateOnly, formatter);
		date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	public Date getDate() {
		return date;
	}
}
