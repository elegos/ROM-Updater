package org.elegosproject.romupdater;

import org.elegosproject.romupdater.types.RepoList;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

public class RepositoriesList extends ROMSuperActivity {
	private static final String TAG = "RomUpdater[RepositoriesList]";
	private static String repoUrl;
	private RepoList[] rawList;
	private ExpandableListView theList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.repo_list);
		theList = (ExpandableListView) findViewById(R.id.repositoriesExpandableList);

		repoUrl = getString(R.string.all_repos_url);
		
		new DownloadJSON().execute(repoUrl);
		
		// select the repository, do some checks, set it as repo url
		theList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					final int groupPosition, final int childPosition, long id) {
				AlertDialog.Builder alert = new AlertDialog.Builder(RepositoriesList.this);
				alert.setCancelable(false);
				
				// Model matches, it's a valid candidate
				if(rawList[groupPosition].getModel().equals(SharedData.LOCAL_MODEL)) {
					alert.setMessage(getString(R.string.apply_repo));
					alert.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(RepositoriesList.this);
							Editor editor = prefs.edit();
							String url = rawList[groupPosition].getRepositories()[childPosition].getUrl();

							// trim the last character until it finishes with "/"
							if (url.contains("/") && !url.contains("json") && !url.contains("?")) {
								url = url.substring(0, url.lastIndexOf("/")+1);
							}
							// push the url in the setting
							editor.putString("repository_url", url);
							editor.commit();

							Toast t = Toast.makeText(RepositoriesList.this, getString(R.string.repository_changed_toast)+" ("+url+")",Toast.LENGTH_LONG);
							t.show();
							dialog.dismiss();
							finish();
						}
					});
					alert.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
				} else {
					// phone model mismatch, throw an error
					alert.setMessage(getString(R.string.repo_phone_mismatch));
					alert.setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
				}
				alert.create().show();
				
				return false;
			}
		});
	}
	
	@Override
	void onJSONDataDownloaded(Boolean success) {
		if(!success)
			return;
		/*
		// alert the user about the list nature
		AlertDialog.Builder info = new AlertDialog.Builder(this);
		info.setTitle(getString(R.string.important))
			.setMessage(getString(R.string.info_shared_repo))
			.setCancelable(false)
			.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		info.create().show();
		*/

		JSONParser parser = new JSONParser();
		rawList = parser.getRepositoriesFromJSON();
		RepositoryExpandableListAdapter adapter = new RepositoryExpandableListAdapter(rawList);
		
		theList.setAdapter(adapter);
	}
	
	public class RepositoryExpandableListAdapter extends BaseExpandableListAdapter {
		private String[] groups;
		private String[][] children;
		
		public RepositoryExpandableListAdapter(RepoList[] theList) {
			int k, i = 0;
			
			groups = new String[theList.length];
			children = new String[theList.length][];
			
			for(i = 0; i < theList.length; i++) {
				groups[i] = theList[i].getModel();
				children[i] = new String[theList[i].getRepositories().length];

				for(k = 0; k < theList[i].getRepositories().length; k++) {
					children[i][k] =  theList[i].getRepositories()[k].getName();
				}
			}
		}
		
		public Object getChild(int groupPosition, int childPosition) {
			return children[groupPosition][childPosition];
		}

		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			TextView textView = getGenericView();
			textView.setText(getChild(groupPosition, childPosition).toString());
			
			return textView;
		}

		public int getChildrenCount(int groupPosition) {
			return children[groupPosition].length;
		}

		public Object getGroup(int groupPosition) {
			return groups[groupPosition];
		}

		public int getGroupCount() {
			return groups.length;
		}

		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			TextView textView = getGenericView();
			textView.setText(getGroup(groupPosition).toString());
			return textView;
		}

		public boolean hasStableIds() {
			return true;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
		
		public TextView getGenericView() {
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT, 74);
			
			TextView textView = new TextView(RepositoriesList.this);
			textView.setLayoutParams(lp);
			textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			textView.setPadding(56, 0, 0, 0);
			
			return textView;
		}
		
	}
}
