/*========================================================================
  VES --- VTK OpenGL ES Rendering Toolkit

      http://www.kitware.com/ves

  Copyright 2011 Kitware, Inc.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 ========================================================================*/

#include <jni.h>
#include <sys/types.h>
#include <android/log.h>

#include <vesKiwiViewerApp.h>
#include <vesKiwiCameraSpinner.h>

#include <vtksys/SystemTools.hxx>
#include <vtkTimerLog.h>

#include <cassert>
#include <fstream>

#include <json/json.h>

#include <vtkIterativeClosestPointTransform.h>

#include <vtkVersion.h>
#include <vtkSmartPointer.h>
#include <vtkTransform.h>
#include <vtkVertexGlyphFilter.h>
#include <vtkPoints.h>
#include <vtkPolyData.h>
#include <vtkCellArray.h>
#include <vtkTransformPolyDataFilter.h>
#include <vtkLandmarkTransform.h>
#include <vtkMath.h>
#include <vtkMatrix4x4.h>
#include <vtkXMLPolyDataWriter.h>
#include <vtkPolyDataMapper.h>
#include <vtkActor.h>
#include <vtkRenderWindow.h>
#include <vtkRenderer.h>
#include <vtkRenderWindowInteractor.h>
#include <vtkXMLPolyDataReader.h>
#include <vtkProperty.h>


#define  LOG_TAG    "MADN3SController"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

// This include uses the LOGI, LOGW, LOGE macro definitions
#include "vtkAndroidOutputWindow.h"

namespace {

class AndroidAppState {
public:

  AndroidAppState() : builtinDatasetIndex(-1) { }

  int builtinDatasetIndex;
  std::string currentDataset;
  vesVector3f cameraPosition;
  vesVector3f cameraFocalPoint;
  vesVector3f cameraViewUp;
};

//----------------------------------------------------------------------------
vesKiwiViewerApp* app;
AndroidAppState appState;

int lastFps;
int fpsFrames;
double fpsT0;

//----------------------------------------------------------------------------
bool testLog(const std::string& message){
	LOGI("Native Testing Log: %s", message.c_str());
	Json::Value root;
	Json::Reader reader;
	bool parsingSuccessful = reader.parse( message, root);
	if ( !parsingSuccessful )
	{
	    std::cout  << "Failed to parse configuration\n"
	               << reader.getFormattedErrorMessages();
	    return false;
	}

	std::string result = root.get("hello", "midget" ).asString();
	LOGI("Native JSONParsing Result: %s",  result.c_str());

	return true;
}

//----------------------------------------------------------------------------
bool doProcess(const std::string& pointsJsonStr){
	LOGI("Native doProcess. jsonReceived length: %d ", pointsJsonStr.length());
	Json::Value root;
	Json::Reader reader;
	bool parsingSuccessful = reader.parse(pointsJsonStr, root);
	if ( !parsingSuccessful ) {
	    std::cout  << "Failed to parse JSONObject\n"
	               << reader.getFormattedErrorMessages();
	    LOGI("Native doProcess. Failed to parse configuration.");
	    return false;
	}

	LOGI("Native doProcess. Obteniendo name.");
	std::string projectName = root.get("name", "not_set" ).asString();
	LOGI("Native doProcess. name: %s", projectName.c_str());
	LOGI("Native doProcess. Obteniendo pictures.");
	Json::Value pictures = root["pictures"];
//	LOGI("Native doProcess. pictures parseadas");
//	LOGI("Native doProcess. pictures type: %s", pictures.type());
	LOGI("Native doProcess. pictures length: %d", pictures.size());
	Json::Value picture;
	Json::Value left;
	Json::Value right;
	Json::Value pointsRight;
	Json::Value pointsLeft;
	Json::Value point;

	LOGI("Native doProcess. Empezando for de pictures.");
	for(int i = 0; i < pictures.size(); i++){
		picture = pictures[i];
		left = picture["left"];
		right = picture["right"];
		pointsLeft = left["points"];
		pointsRight = right["points"];
		LOGI("Native doProcess. Obteniendo left points de %d.", i);
		for(int j = 0; j < pointsLeft.size(); j++){
			point = pointsLeft[j];
			LOGI("Native doProcess. point left(%d, %d): (%d,%d) ", i, j, point["x"].asInt(), point["y"].asInt());
		}

		LOGI("Native doProcess. Obteniendo right points de %d.", i);
		for(int j = 0; j < pointsRight.size(); j++){
			point = pointsRight[j];
			LOGI("Native doProcess. point right(%d, %d): (%d,%d) ", i, j, point["x"].asInt(), point["y"].asInt());
		}
	}
	LOGI("Native doProcess. For done moddafocka!.");

	LOGI("Native doProcess. instanciando ICP de la muerte.");
	vtkSmartPointer<vtkIterativeClosestPointTransform> icp =
	      vtkSmartPointer<vtkIterativeClosestPointTransform>::New();
	LOGI("Native doProcess. el todopoderoso ICP nos perdonó la vida.");
	return icp != NULL;
}

//----------------------------------------------------------------------------
void resetView()
{
  if (appState.builtinDatasetIndex >= 0) {
    app->applyBuiltinDatasetCameraParameters(appState.builtinDatasetIndex);
  }
  else {
    app->resetView();
  }
}

//----------------------------------------------------------------------------
bool loadDataset(const std::string& filename, int builtinDatasetIndex)
{
  LOGI("loadDataset(%s)", filename.c_str());

  appState.currentDataset = filename;
  appState.builtinDatasetIndex = builtinDatasetIndex;

  bool result = app->loadDataset(filename);
  if (result) {
    resetView();
  }
  return result;
}

//----------------------------------------------------------------------------
void clearExistingDataset()
{
  appState.currentDataset = std::string();
  appState.builtinDatasetIndex = -1;
}

//----------------------------------------------------------------------------
void storeCameraState()
{
  appState.cameraPosition = app->cameraPosition();
  appState.cameraFocalPoint = app->cameraFocalPoint();
  appState.cameraViewUp = app->cameraViewUp();
}

//----------------------------------------------------------------------------
void restoreCameraState()
{
  app->setCameraPosition(appState.cameraPosition);
  app->setCameraFocalPoint(appState.cameraFocalPoint);
  app->setCameraViewUp(appState.cameraViewUp);
}

//----------------------------------------------------------------------------
bool setupGraphics(int w, int h)
{

  // The app may lose its GL context and so it
  // calls setupGraphics when the app resumes.  We don't have an
  // easy way to re-initialize just the GL resources, so the strategy
  // for now is to delete the whole app instance and then restore
  // the current dataset and camera state.
  bool isResume = app != NULL;
  if (isResume) {
    storeCameraState();
    delete app; app = 0;
    isResume = true;
  }

  // Pipe VTK messages into the android log
  vtkAndroidOutputWindow::Install();

  app = new vesKiwiViewerApp();
  app->initGL();
  app->resizeView(w, h);

  if (isResume && !appState.currentDataset.empty()) {
    app->loadDataset(appState.currentDataset);
    restoreCameraState();
  }

  fpsFrames = 0;
  fpsT0 = vtkTimerLog::GetUniversalTime();

  return true;
}

}; // end namespace
//----------------------------------------------------------------------------


//----------------------------------------------------------------------------
extern "C" {
  JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_init(JNIEnv * env, jobject obj,  jint width, jint height);
  JNIEXPORT jboolean JNICALL Java_org_madn3s_controller_ves_KiwiNative_testLog(JNIEnv * env, jobject obj,  jstring message);
  JNIEXPORT jboolean JNICALL Java_org_madn3s_controller_ves_KiwiNative_doProcess(JNIEnv * env, jobject obj,  jstring pointsJsonStr);
  JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_reshape(JNIEnv * env, jobject obj,  jint width, jint height);
  JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_handleSingleTouchPanGesture(JNIEnv * env, jobject obj,  jfloat dx, jfloat dy);
  JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_handleTwoTouchPanGesture(JNIEnv * env, jobject obj,  jfloat x0, jfloat y0, jfloat x1, jfloat y1);
  JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_handleTwoTouchPinchGesture(JNIEnv * env, jobject obj,  jfloat scale);
  JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_handleTwoTouchRotationGesture(JNIEnv * env, jobject obj,  jfloat rotation);
  JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_handleSingleTouchDown(JNIEnv * env, jobject obj,  jfloat x, jfloat y);
  JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_handleSingleTouchUp(JNIEnv * env, jobject obj);
  JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_handleSingleTouchTap(JNIEnv * env, jobject obj,  jfloat x, jfloat y);
  JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_handleDoubleTap(JNIEnv * env, jobject obj,  jfloat x, jfloat y);
  JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_handleLongPress(JNIEnv * env, jobject obj,  jfloat x, jfloat y);
  JNIEXPORT jboolean JNICALL Java_org_madn3s_controller_ves_KiwiNative_render(JNIEnv * env, jobject obj);
  JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_resetCamera(JNIEnv * env, jobject obj);
  JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_stopInertialMotion(JNIEnv * env, jobject obj);
  JNIEXPORT jstring JNICALL Java_org_madn3s_controller_ves_KiwiNative_getDatasetName(JNIEnv* env, jobject obj, jint offset);
  JNIEXPORT jstring JNICALL Java_org_madn3s_controller_ves_KiwiNative_getDatasetFilename(JNIEnv* env, jobject obj, jint offset);
  JNIEXPORT jint JNICALL Java_org_madn3s_controller_ves_KiwiNative_getNumberOfBuiltinDatasets(JNIEnv* env, jobject obj);
  JNIEXPORT jint JNICALL Java_org_madn3s_controller_ves_KiwiNative_getDefaultBuiltinDatasetIndex(JNIEnv* env, jobject obj);
  JNIEXPORT jboolean JNICALL Java_org_madn3s_controller_ves_KiwiNative_getDatasetIsLoaded(JNIEnv* env, jobject obj);
  JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_clearExistingDataset(JNIEnv * env, jobject obj);
  JNIEXPORT jboolean JNICALL Java_org_madn3s_controller_ves_KiwiNative_loadDataset(JNIEnv* env, jobject obj, jstring filename, int builtinDatasetIndex);
  JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_checkForAdditionalDatasets(JNIEnv* env, jobject obj, jstring storageDir);
  JNIEXPORT jstring JNICALL Java_org_madn3s_controller_ves_KiwiNative_getLoadDatasetErrorTitle(JNIEnv* env, jobject obj);
  JNIEXPORT jstring JNICALL Java_org_madn3s_controller_ves_KiwiNative_getLoadDatasetErrorMessage(JNIEnv* env, jobject obj);

  JNIEXPORT jint JNICALL Java_org_madn3s_controller_ves_KiwiNative_getNumberOfTriangles(JNIEnv* env, jobject obj);
  JNIEXPORT jint JNICALL Java_org_madn3s_controller_ves_KiwiNative_getNumberOfLines(JNIEnv* env, jobject obj);
  JNIEXPORT jint JNICALL Java_org_madn3s_controller_ves_KiwiNative_getNumberOfVertices(JNIEnv* env, jobject obj);
  JNIEXPORT jint JNICALL Java_org_madn3s_controller_ves_KiwiNative_getFramesPerSecond(JNIEnv* env, jobject obj);
  
  // we have a libtiff dependency because we want to use vtk's tiff image reader.
  // libtiff requires lfind, but it is missing in Android's libc
  void* lfind( const void * key, const void * base, size_t num, size_t width, int (*fncomparison)(const void *, const void * ) )
  {
    char* Ptr = (char*)base;
    for ( size_t i = 0; i != num; i++, Ptr+=width ) {
      if ( fncomparison( key, Ptr ) == 0 ) return Ptr;
    }
    return NULL;
  }
};

//-----------------------------------------------------------------------------
JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_init(JNIEnv * env, jobject obj,  jint width, jint height)
{
  LOGI("setupGraphics(%d, %d)", width, height);
  setupGraphics(width, height);
}

JNIEXPORT jboolean JNICALL Java_org_madn3s_controller_ves_KiwiNative_testLog(JNIEnv * env, jobject obj,  jstring message)
{
	const char *javaStr = env->GetStringUTFChars(message, NULL);
	LOGI("JNIExport Testing Log: %s", javaStr);

	  if (javaStr) {
		std::string messageStr = javaStr;
		return testLog(messageStr);
	  }
	return false;
}

JNIEXPORT jboolean JNICALL Java_org_madn3s_controller_ves_KiwiNative_doProcess(JNIEnv * env, jobject obj, jstring pointsJsonStr)
{
	const char *javaStr = env->GetStringUTFChars(pointsJsonStr, NULL);
	LOGI("JNIExport Java_org_madn3s_controller_ves_KiwiNative_doProcess. pointsJsonStr length: %d", strlen(javaStr));
	if (javaStr) {
		std::string messageStr = javaStr;
		LOGI("JNIExport Java_org_madn3s_controller_ves_KiwiNative_doProcess. messageStr length: %d", messageStr.length());
		return doProcess(messageStr);
	}
	return false;
}

JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_reshape(JNIEnv * env, jobject obj,  jint width, jint height)
{
  LOGI("reshape(%d, %d)", width, height);
  app->resizeView(width, height);
}

JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_handleSingleTouchPanGesture(JNIEnv * env, jobject obj,  jfloat dx, jfloat dy)
{
  app->handleSingleTouchPanGesture(dx, dy);
}

JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_handleTwoTouchPanGesture(JNIEnv * env, jobject obj,  jfloat x0, jfloat y0, jfloat x1, jfloat y1)
{
  app->handleTwoTouchPanGesture(x0, y0, x1, y1);
}

JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_handleTwoTouchPinchGesture(JNIEnv * env, jobject obj,  jfloat scale)
{
  app->handleTwoTouchPinchGesture(scale);
}

JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_handleTwoTouchRotationGesture(JNIEnv * env, jobject obj,  jfloat rotation)
{
  app->handleTwoTouchRotationGesture(rotation);
}

JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_handleSingleTouchDown(JNIEnv * env, jobject obj,  jfloat x, jfloat y)
{
  app->handleSingleTouchDown(x, y);
}

JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_handleSingleTouchUp(JNIEnv * env, jobject obj)
{
  app->handleSingleTouchUp();
}

JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_handleSingleTouchTap(JNIEnv * env, jobject obj,  jfloat x, jfloat y)
{
  app->handleSingleTouchTap(x, y);
}

JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_handleDoubleTap(JNIEnv * env, jobject obj,  jfloat x, jfloat y)
{
  app->handleDoubleTap(x, y);
}

JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_handleLongPress(JNIEnv * env, jobject obj,  jfloat x, jfloat y)
{
  app->handleLongPress(x, y);
}

JNIEXPORT jboolean JNICALL Java_org_madn3s_controller_ves_KiwiNative_render(JNIEnv * env, jobject obj)
{
  double currentTime = vtkTimerLog::GetUniversalTime();
  double dt = currentTime - fpsT0;
  if (dt > 1.0) {
    lastFps = static_cast<int>(fpsFrames/dt);
    fpsFrames = 0;
    fpsT0 = currentTime;
    //LOGI("fps: %d", lastFps);
  }

  //LOGI("render");
  app->render();

  fpsFrames++;

  return app->cameraSpinner()->isEnabled() || app->isAnimating();
}

JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_resetCamera(JNIEnv * env, jobject obj)
{
  resetView();
}

JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_stopInertialMotion(JNIEnv * env, jobject obj)
{
  app->haltCameraRotationInertia();
}

JNIEXPORT jstring JNICALL Java_org_madn3s_controller_ves_KiwiNative_getDatasetName(JNIEnv* env, jobject obj, jint offset)
{
  std::string name = app->builtinDatasetName(offset);
  const char* nameForOutput = name.c_str();
  return(env->NewStringUTF(name.c_str()));
}

JNIEXPORT jstring JNICALL Java_org_madn3s_controller_ves_KiwiNative_getDatasetFilename(JNIEnv* env, jobject obj, jint offset)
{
  std::string name = app->builtinDatasetFilename(offset);
  const char* nameForOutput = name.c_str();
  return(env->NewStringUTF(name.c_str()));
}

JNIEXPORT jint JNICALL Java_org_madn3s_controller_ves_KiwiNative_getNumberOfBuiltinDatasets(JNIEnv* env, jobject obj)
{
  return app->numberOfBuiltinDatasets();
}

JNIEXPORT jint JNICALL Java_org_madn3s_controller_ves_KiwiNative_getDefaultBuiltinDatasetIndex(JNIEnv* env, jobject obj)
{
  return app->defaultBuiltinDatasetIndex();
}

JNIEXPORT jboolean JNICALL Java_org_madn3s_controller_ves_KiwiNative_getDatasetIsLoaded(JNIEnv* env, jobject obj)
{
  return !appState.currentDataset.empty();
}

JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_clearExistingDataset(JNIEnv * env, jobject obj)
{
  clearExistingDataset();
}

JNIEXPORT jboolean JNICALL Java_org_madn3s_controller_ves_KiwiNative_loadDataset(JNIEnv* env, jobject obj, jstring filename, jint builtinDatasetIndex)
{
  const char *javaStr = env->GetStringUTFChars(filename, NULL);
  if (javaStr) {
    std::string filenameStr = javaStr;
    env->ReleaseStringUTFChars(filename, javaStr);
    return loadDataset(filenameStr, builtinDatasetIndex);
  }
  return false;
}

JNIEXPORT void JNICALL Java_org_madn3s_controller_ves_KiwiNative_checkForAdditionalDatasets(JNIEnv* env, jobject obj, jstring storageDir)
{
  const char *javaStr = env->GetStringUTFChars(storageDir, NULL);
  if (javaStr) {
    std::string storageDirStr = javaStr;
    env->ReleaseStringUTFChars(storageDir, javaStr);
    app->checkForAdditionalData(storageDirStr);
  }
}

JNIEXPORT jstring JNICALL Java_org_madn3s_controller_ves_KiwiNative_getLoadDatasetErrorTitle(JNIEnv* env, jobject obj)
{
  std::string str = app->loadDatasetErrorTitle();
  return env->NewStringUTF(str.c_str());
}

JNIEXPORT jstring JNICALL Java_org_madn3s_controller_ves_KiwiNative_getLoadDatasetErrorMessage(JNIEnv* env, jobject obj)
{
  std::string str = app->loadDatasetErrorMessage();
  return env->NewStringUTF(str.c_str());
}

JNIEXPORT jint JNICALL Java_org_madn3s_controller_ves_KiwiNative_getNumberOfTriangles(JNIEnv* env, jobject obj)
{
  return app->numberOfModelFacets();
}

JNIEXPORT jint JNICALL Java_org_madn3s_controller_ves_KiwiNative_getNumberOfLines(JNIEnv* env, jobject obj)
{
  return app->numberOfModelLines();
}

JNIEXPORT jint JNICALL Java_org_madn3s_controller_ves_KiwiNative_getNumberOfVertices(JNIEnv* env, jobject obj)
{
  return app->numberOfModelVertices();
}

JNIEXPORT jint JNICALL Java_org_madn3s_controller_ves_KiwiNative_getFramesPerSecond(JNIEnv* env, jobject obj)
{
  return lastFps;
}
