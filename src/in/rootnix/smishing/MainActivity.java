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
	protected void onStart() { //앱이 시작되면
		// TODO Auto-generated method stub
		super.onStart();
		getAllSMS();
		getContacts(); //연락처 
//		findThumbList(); //사진의 썸리스트를 갖고 온다.
	}
	
	private void getAllSMS() {
		String[] reqCols = new String[]{"address", "body"};
		//Cursor
		//SQLlite로 저장이되는데, Cursor는 DB를 제어하는 것이다.
		//inbox를 SENT 보낸메시지,  DRAFT 임시 보관함이 나옴.
		//reqCol 부분이 address와 body로 설정되어있다. address는 핸드폰번호, body는 내용
		//reqCols 부분은 SELECT (id, password) 이 부분이다.
		Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), reqCols, null, null, null);
		//cursor에 결과값이 들어온다.
		
		cursor.moveToFirst(); //cursor의 맨 앞으로 이동하라(데이터의 맨앞으로 오라)
		
		String strMsg = null;

		if(cursor.getCount() > 0) { //쿼리를 날려서 넘어온 데이터의 갯수를 세어준다. cursor.getCount()/
			//0인데 데이터를 읽어오면앱이 죽는다.
			do{
			   String msgData = "";
			   
			   for(int idx=0;idx<cursor.getColumnCount();idx++)
			   {
			       msgData += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx);
			       //address:전번 들어오고 그 다음 body: 내용 이 들어옴
			   }
			   Log.d("MJ", "SMS: "+msgData);
			   strMsg += msgData+"\n";
			   tvSMS.setText(strMsg);
			}while(cursor.moveToNext()); //커서를 한칸 내림
		}
		
	}
	
	private void getContacts() {//CONTENT_URI는 연락처를 가르킨다
		String strContacts = null;
		ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                  String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                  String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                  if (Integer.parseInt(cur.getString( //string을 인트로 바꿔서 0인지 아닌지 확인해준다.
                        cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) { //has_phone_number는 boolean 값을 가져온다 있냐없냐로
                     Cursor pCur = cr.query(
                               ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                               null,
                               ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                               new String[]{id}, null);
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

		String[] projection = { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.DATA };

		Cursor imageCursor = getContentResolver().query( //EXTERNAL SD카드에 저장이되기 때문에(사진이) EXTERNAL_CONTENT_URI를 사용한다.
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
				null, MediaStore.Images.Media.DATE_ADDED + " desc ");
		// MediaStore.Images.Media.DATE_ADDED + " desc " : 정렬이다. ORDER BY 생성시간(최근)
		//최근에 찍은 ID, DATA로 정렬된다.
		if (imageCursor != null && imageCursor.getCount() > 0) {
			int imageIDCol = imageCursor
					.getColumnIndex(MediaStore.Images.Media._ID);
			int imageDataCol = imageCursor
					.getColumnIndex(MediaStore.Images.Media.DATA);
			while (imageCursor.moveToNext()) {
				ThumbImageInfo thumbInfo = new ThumbImageInfo();

				thumbInfo.setId(imageCursor.getString(imageIDCol));
				thumbInfo.setData(imageCursor.getString(imageDataCol));
				thumbInfo.setCheckedState(false); //없어도 된다

				Log.d("MJ", "id: "+imageCursor.getString(imageIDCol)+", data: " + imageCursor.getString(imageDataCol));
//				thumbInfo.setData(imageCursor.getString(imageDataCol));
				mThumbImageInfoList.add(thumbInfo);
				//이미지 값을 봅으면 그 이미지의 경로가 뽑혀온다.
				returnValue++;
			}
		}
		imageCursor.close();
		return returnValue;
	}
	
}
