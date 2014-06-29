package in.rootnix.smishing;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {

	Context mContext;
	TextView tvSMS;
	TextView tvContacts;
	ArrayList<ThumbImageInfo> mThumbImageInfoList = new ArrayList<ThumbImageInfo>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		init();
	}
	
	private void init() {
		tvSMS = (TextView)findViewById(R.id.tvSMS);
		tvContacts = (TextView)findViewById(R.id.tvContacts);
		tvSMS.setMovementMethod(new ScrollingMovementMethod());
		tvContacts.setMovementMethod(new ScrollingMovementMethod());
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		getAllSMS();
		getContacts();
//		findThumbList();
	}
	
	private void getAllSMS() {
		//address, body 는 약속되어진 값
		String[] reqCols = new String[]{"address", "body"};
		
		//Cusor 안드로이드에는 sql lite 라는 DB가 있음 커서는 sql lite를 읽어오는 애임
		//inbox 대신 다른 옵션 가능, reCols 부분이 select (id,password) from table~~~ ()이 부분임 
		Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), reqCols, null, null, null);
		
		//데이터의 첫번째 값으로 이동하라는 뜻
		cursor.moveToFirst();
		
		
		String strMsg = null;

		
		if(cursor.getCount() > 0) {
			do{
			   String msgData = "";
			   
			   for(int idx=0;idx<cursor.getColumnCount();idx++)
			   {
				   //address : ~~~~
				   //body : ~~~~
			       msgData += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx);
			   }
			   Log.d("MJ", "SMS: "+msgData);
			   strMsg += msgData+"\n";
			   
			   //text view
			   tvSMS.setText(strMsg);
			}while(cursor.moveToNext());
		}
		
	}
	
	private void getContacts() {
		
		String strContacts = null;
		ContentResolver cr = getContentResolver();
		//CONTENT_URI : 연락처  (제조사마다 다른걸 미리 정의해놓음)
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
        
        
        if (cur.getCount() > 0) {
        	
            while (cur.moveToNext()) {
            	
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                
                  
                  //번호가 있는지 없는지 판별 (이름만 있고 번호가 없는 경우)
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                	
                	  //where 문임
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",new String[]{id}, null);
                    while (pCur.moveToNext()) {
                    	
                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.d("MJ", name+":"+phoneNo);
                        strContacts += name+":"+phoneNo+"\n";
                    }
                    pCur.close();
                }
                  tvContacts.setText(strContacts);
            }
        }
        
        
	}

	
	private long findThumbList() {
		
		long returnValue = 0;

		String[] projection = { MediaStore.Images.Media._ID,MediaStore.Images.Media.DATA };

		Cursor imageCursor = getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
				null, MediaStore.Images.Media.DATE_ADDED + " desc ");

		if (imageCursor != null && imageCursor.getCount() > 0) {
			int imageIDCol = imageCursor
					.getColumnIndex(MediaStore.Images.Media._ID);
			int imageDataCol = imageCursor
					.getColumnIndex(MediaStore.Images.Media.DATA);
			while (imageCursor.moveToNext()) {
				ThumbImageInfo thumbInfo = new ThumbImageInfo();

				thumbInfo.setId(imageCursor.getString(imageIDCol));
				thumbInfo.setData(imageCursor.getString(imageDataCol));
				thumbInfo.setCheckedState(false);//무시

				Log.d("MJ", "id: "+imageCursor.getString(imageIDCol)+", data: " + imageCursor.getString(imageDataCol));
//				thumbInfo.setData(imageCursor.getString(imageDataCol));
				
				//정의한 리스트형식만 들어갈수있음
				mThumbImageInfoList.add(thumbInfo);
				returnValue++;
			}
		}
		imageCursor.close();
		return returnValue;
	}
	
}
