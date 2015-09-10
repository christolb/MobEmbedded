A social networking App developed for android smartphones, that brings the two worlds of Social networking and augmented reality together.



Steps for setting up Vuforia Samples:
1. Import Vuforia Samples into Android studio using  
https://developer.vuforia.com/forum/android/using-android-studio-vuforia
set compileSdkVersion 21  in build.gradle of the Module:App  immediately after importing. Then proceed to vuforia.jar part in the above link.


2. add
apply plugin:'java' 
in the first line of build.gradle of the Project

3. give clean build and App should compile properly.
