package com.util;

import java.awt.Color;

public class Constants {
	public static final int WIDTH = 1024;
	public static final int HEIGHT = 768;

	public static final int SPLASH_SCREEN_STATE = 0;
	public static final int TITLE_STATE = 1;
	public static final int LOGIN_STATE = 2;
	public static final int SIGNUP_STATE = 3;
	public static final int INTRO_STATE = 4;
	public static final int PROFILE_STATE = 5;
	public static final int CHANGE_PASSWORD_STATE = 6;
	public static final int RESET_PASSWORD_STATE = 7;
	public static final int EDIT_PROFILE_STATE = 8;
	public static final int CHOOSE_MODULE_STATE = 9;
	public static final int MODULE_OVERVIEW_STATE = 10;
	public static final int EXIT_STATE = 11;
	public static final int CLASS_RECORD_STATE = 27; //TODO replace value when merged with new features

	/**********************************************************************************/
	/** CONSTANTS FOR GAME TYPES **/
	/**********************************************************************************/

	public static final int LESSON_OVERVIEW_STATE = 11;
	public static final int MULTIPLE_CHOICE_STATE = 12;
	public static final int FIND_THE_ERROR_STATE = 13;
	public static final int CLOZE_WCHOICES_STATE = 14;
	public static final int CLOZE_WOCHOICES_STATE = 15;
	public static final int FTB_WCHOICES_STATE = 16;
	public static final int FTB_WOCHOICES_STATE = 17;
	public static final int LECTURE_STATE = 18;
	public static final int SPEECH_STATE = 19;
	public static final int PRACTICE_DRILLS_STATE = 20;
	

	public static final int DB_UPLOAD = 0;
	public static final int DB_LOGIN = 1;
	public static final int DB_RESET = 2;
	public static final int DB_SIGNUP = 3;

	
	public static final String MULTIPLE_CHOICE = "multiplechoice";
	public static final String FIND_THE_ERROR = "findtheerror";
	public static final String CLOZE_WCHOICES = "clozewithchoices";
	public static final String CLOZE_WOCHOICES = "clozewithoutchoices";
	public static final String FTB_WCHOICES = "fillintheblankswithchoices";
	public static final String FTB_WOCHOICES = "fillintheblankswithoutchoices";

	public static final String SP_REPEATWORD = "repeatword";
	public static final String SP_REPEATSENTENCE = "repeatsentence";
	public static final String SP_READSENTENCE = "readsentence";
	public static final String SP_SAYWORD = "sayword";
	public static final String SP_CONVERSATION = "conversation";
	public static final String SP_SHORTPARAGRAPH = "readshortparagraph";
	public static final String SP_LONGPARAGRAPH = "readlongparagraph";
	public static final String SP_QANDA = "qanda";
	public static final String SP_CALLSIMULATION = "callsimulation";
	public static final int SAYWORD_MAXRECORDTIME = 4; // 4 seconds

	public static final String SP_SC_CHECKSTRESS = "checkstress";
	public static final String SP_SC_CHECKPHRASING = "checkphrasing";
	public static final String SP_SC_CHECKEMPHASIS = "checkemphasis";
	public static final String SP_SC_CHECKINTONATION = "checkintonation";
	public static final String SP_SC_CHECKALL = "checkall";
	
	/** Control panel status constants **/
	public final static int SP_PLAYING_SAMPLE = 1;
	public final static int SP_PLAYING_RECORDED = 2;
	public final static int SP_RECORDING = 3;
	public final static int SP_RECORDING_STOPPED = 4;
	public final static int SP_PLAYING_SAMPLE_STOPPED = 5;
	public final static int SP_PLAYING_RECORDED_STOPPED = 6;
	public final static int SP_FINISHED_PLAYING_SAMPLE = 7;
	public final static int SP_FINISHED_PLAYING_RECORDED = 8;
	public final static int SP_NO_RECORDED = 9;
	public final static int SP_TURN_ON_MIC = 10;
	public final static int SP_PLAYING_QUESTION = 11;
	public final static int SP_PLAYING_QUESTION_STOPPED = 12;
	public final static int SP_FINISHED_PLAYING_QUESTION = 13;
	public final static int SP_CHECKING = 15;
	public final static int SP_FINISHED_CHECKING = 16;


	public static final String CONFIG_INITSPEECH = "initSpeech";
	public static final String CONFIG_INITGV = "initG&V";
	
	public static final int LEC_DEFAULT = 0;
	public static final int LEC_INTRO = 1;
	public static final int LEC_AUDIO = 2;
	public static final int LEC_SPEECH = 3;
	
	public static final String SPEECH = "speech";

	
	public static float PASSING_GRADE = 0.6f;
	
	/**********************************************************************************/
	/** CONSTANTS FOR ICONS **/
	/**********************************************************************************/

	public static final String icon = "assets/icons/LEAP_32.ico";

	/**********************************************************************************/
	/** CONSTANTS FOR BACKGROUND **/
	/**********************************************************************************/
	public static final int BG_X = 0;
	public static final int BG_Y = 0;

	public static final String BG_1 = "bg_01";
	public static final String BG_2 = "bg_02";
	public static final String BG_3 = "bg_03";
	public static final String BG_1_BOX = "bg_01_box";
	public static final String BG_2_BOX = "bg_02_box";
	public static final String BG_3_BOX = "bg_03_box";

	/**********************************************************************************/
	/** CONSTANTS FOR AVATAR **/
	/**********************************************************************************/
	public static final int BOY_1 = 0;
	public static final int BOY_2 = 1;
	public static final int BOY_3 = 2;
	public static final int GIRL_1 = 3;
	public static final int GIRL_2 = 4;

	/**********************************************************************************/
	/** CONSTANTS FOR CONTENT BOX **/
	/**********************************************************************************/
	public static final int CONTENT_X = 310;
	public static final int CONTENT_Y = 200;
	public static final int CONTENT_WIDTH = 664;
	public static final int CONTENT_HEIGHT = 420;
	public static final int CONTENT_LF_MARGIN = 200;

	/**********************************************************************************/
	/** FOR CLOZEBOX GAME **/
	/**********************************************************************************/
	public static final int BLANK_X_PAD = 5;
	public static final int BLANK_Y_PAD = 7;
	public static final int PASSAGE_Y_INC = 40;

	public static final int CHECK_X_PAD = 10;
	public static final int CHECK_Y_PAD = 15;

	public static final int BLANK_WIDTH = 30;
	public static final int BLANK_PAD = 5;
	public static final int BLANK_HEIGHT = 36;
	public static final int BLANK_SIZE = 51;
	public static final int PUNC_START_WIDTH = 5;
	public static final int PUNC_END_WIDTH = 10;

	public static final int TB_UP_X = 300;
	public static final int TB_UP_Y = 200;
	public static final int TB_UP_Y_PAD = 70;
	public static final int TB_DOWN_X = 300;
	public static final int TB_DOWN_Y = 600;
	public static final int TB_DOWN_Y_PAD = 16;

	public static final int WORD_FONT_SIZE = 15;

	public static final int SCROLL_INC = 3;

	public static final int WORD_X = 310;
	public static final int WORD_Y = 190;
	public static final int WORD_INC = 35;

	/**********************************************************************************/
	/** FOR FILL IN THE BLANKS GAME **/
	/**********************************************************************************/

	public static final int FTB_BLANK_Y_PAD = 30;
	public static final int FTB_PASSAGE_Y_INC = 45;
	public static final int FTB_CHECK_Y_PAD = 25;

	public static final int SCREEN_WIDTH = 1024;
	public static final int SCREEN_HEIGHT = 700;

	/**********************************************************************************/
	/** FONTS **/
	/**********************************************************************************/
	public static final String BG_LANG = "bg_language";
	public static final String BG_PRESENTATION = "bg_presentation";

	public static final String FNT_MYRIAD_REG = "/assets/fonts/myriad/MyriadPro-Regular.ttf";
	public static final String FNT_MYRIAD_SBOLD = "/assets/fonts/myriad/MyriadPro-Semibold.ttf";
	public static final String FNT_MYRIAD_BOLDIT = "/assets/fonts/myriad/MyriadPro-BoldIt.otf";
	public static final String FNT_MYRIAD_SBOLDIT = "/assets/fonts/myriad/MyriadPro-SemiboldIt.otf";
	public static final String FNT_MYRIAD_BOLD = "/assets/fonts/myriad/MyriadPro-Bold.ttf";
	public static final String FNT_MYRIAD_ITALIC = "/assets/fonts/myriad/MyriadPro-It.otf";
	public static final String FNT_MYRIAD_LIGHTIT = "/assets/fonts/myriad/MyriadPro-LightIt.otf";
	public static final String FNT_MYRIAD_LIGHT = "/assets/fonts/myriad/MyriadPro-Light.otf";
	public static final String FNT_MYRIAD_BLACK = "/assets/fonts/myriad/MyriadPro-Black.otf";

	public static final String FNT_GROBOLD = "/assets/fonts/grobold/GROBOLD.ttf";

	public static final Color BLUE = new Color(0, 113, 188);


	public static final int REPEATWORD = 1;
	public static final int REPEATREADSENTENCE = 2;
	public static final int READSHORTLONGPARAGRAPH = 3;
	public static final int QANDA_A = 4;
	public static final int QANDA_Q = 5;
	public static final int PDRILL = 6;
	public static final int SAYWORD = 7;

}
