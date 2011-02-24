package br.com.passeionaweb.android.hoursbank;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

public class MonthActivity extends CheckpointListActivity {
	private Dialog addDialog;
	private int month;
	public static final int MENU_SET_MONTH = 20;
	public static final int DIALOG_SET_MONTH = 10;

	protected void fillData() {

		db.open();
		// Get all of the rows from the database and create the item list
		Cursor cursor = db.getMonthCheckpoints(this,month);
		startManagingCursor(cursor);

		CheckpointsView chk = new CheckpointsView(this);

		String[] from = new String[] { CheckpointsView.KEY_DAY, CheckpointsView.KEY_TOTAL,
				CheckpointsView.KEY_BALANCE, CheckpointsView.KEY_IMAGE };
		int[] to = new int[] { R.id.txtCheckPoint, R.id.txtTotalHours, R.id.txtHourBalanceRow,
				R.id.imgCheckpointInOut };
		if (cursor.getCount() > 0) {
			List<HashMap<String, String>> list = chk.cursorToList(cursor, CheckpointsView.MONTH,month);
			SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.checkpoint_row, from, to);
			setListAdapter(adapter);
			findViewById(R.id.layoutContainer).setVisibility(View.VISIBLE);
		}else {
			setListAdapter(null);
		}
		long sum = chk.calculateTotalHours(cursor);
		String totalHours = chk.formatTotalHours(sum);
		((TextView) findViewById(R.id.lblTotalHours)).setText(totalHours);
		findViewById(R.id.layoutBalance).setVisibility(View.VISIBLE);
		((TextView) findViewById(R.id.lblHoursBalance)).setText(chk.formatTotalHours(chk
				.getBalance()));
		if (chk.getBalance() >= 0) {
			((ImageView) findViewById(R.id.imgHoursBalance))
					.setImageResource(R.drawable.ic_btn_round_plus);
		} else {
			((ImageView) findViewById(R.id.imgHoursBalance))
					.setImageResource(R.drawable.ic_btn_round_minus);
		}
		db.close();
	}

	@Override
	protected Dialog createAddDialog() {
		addDialog = new Dialog(this);
		addDialog.setTitle(R.string.dialog_add_checkpoint_title2);
		addDialog.setContentView(R.layout.dialog_new_checkpoint);
		((TimePicker) addDialog.findViewById(R.id.tpAddCheckpoint)).setIs24HourView(true);
		((Button) addDialog.findViewById(R.id.btnDialogAddCheckpointOk))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						int day = ((DatePicker) addDialog.findViewById(R.id.dtAddCheckpoint))
								.getDayOfMonth();
						int month = ((DatePicker) addDialog.findViewById(R.id.dtAddCheckpoint))
								.getMonth();
						int hour = ((TimePicker) addDialog.findViewById(R.id.tpAddCheckpoint))
								.getCurrentHour();
						int minute = ((TimePicker) addDialog.findViewById(R.id.tpAddCheckpoint))
								.getCurrentMinute();
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.DAY_OF_MONTH, day);
						cal.set(Calendar.MONTH, month);
						cal.set(Calendar.HOUR_OF_DAY, hour);
						cal.set(Calendar.MINUTE, minute);
						insertCheckpoint(cal.getTimeInMillis());
						addDialog.dismiss();
					}
				});
		((Button) addDialog.findViewById(R.id.btnDialogAddCheckpointCancel))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						addDialog.dismiss();
					}
				});

		return addDialog;

	}

	@Override
	protected Dialog createEditDialog() {
		// TODO Implement this method
		return null;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// TODO implement this method to edit and delete all checkpoints related
		// to this day
		// DO NOT REMOVE THIS METHOD
		// This method is necessary to avoid the superclass method of creating
		// this context menu
		// DO NOT REMOVE THIS METHOD
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(getApplicationContext(), DayActivity.class);

		String strDay = (String) ((TextView) v.findViewById(R.id.txtCheckPoint)).getText();
		Calendar day = Calendar.getInstance();
		day.set(Calendar.DAY_OF_MONTH, Integer.valueOf(strDay.split(" ")[1].split("/")[0]));
		day.set(Calendar.MONTH, Integer.valueOf(strDay.split(" ")[1].split("/")[1]) - 1);

		intent.putExtra("DAY", day.getTimeInMillis());
		startActivity(intent);
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_SET_MONTH, Menu.NONE, R.string.menu_set_month).setIcon(
				android.R.drawable.ic_menu_month);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case MENU_SET_MONTH:
				showDialog(DIALOG_SET_MONTH);
				break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	private void setMonth(int month) {
		this.month = month;
		fillData();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
			case DIALOG_SET_MONTH:
			return createSetMonthDialog();
			default:
				return super.onCreateDialog(id);
		}
	}

	protected Dialog createSetMonthDialog() {
		return new AlertDialog.Builder(this).setItems(R.array.months,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						setMonth(which);
					}
				}).create();

	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		month = Calendar.getInstance().get(Calendar.MONTH);
		super.onCreate(savedInstanceState);
	}
}
