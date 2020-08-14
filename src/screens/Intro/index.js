import React from 'react';
import {
  Image,
  View,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import { RoundedButton } from '../../components';
import DotsPagination from './DotsPagination';

import step1Pic from './picIntro1.png';
// import step2Pic from './picIntro2.png';
// import step3Pic from './picIntro3.png';
import step4Pic from './picIntro4.png';

const Step1Content = () => (
  <>
    <View style={styles.imgContainer}>
      <Image source={step1Pic} style={styles.img} />
    </View>
    <Text style={styles.header}>
      Contribuie la prevenirea răspândirii Covid-19
    </Text>
    <Text style={styles.info}>
      Află cum poți să contribui la prevenirea răspândirii virusului COVID-19
      folosind telefonul tău.
    </Text>
    <Text style={{ ...styles.info, fontWeight: 'bold' }}>
      Află dacă ai intrat în contact cu o persoană infectată
    </Text>
    <Text style={{ ...styles.info, fontWeight: 'bold' }}>
      Toate datele colectate sunt anonime și nu pot contribui la identificarea
      ta
    </Text>
  </>
);

const Step2Content = () => (
  <>
    <View style={styles.imgContainer}>
      <Image source={step1Pic} style={styles.img} />
    </View>
    <Text style={styles.header}>Permite accesul la locație</Text>
    <Text style={styles.info}>
      Telefonul tău fa ști unde și cu cine ai intrat în contact și dacă această
      persoană a fost diagnosticată pozitiv
    </Text>
    <RoundedButton
      title="PERMITE ACCES"
      containerStyle={styles.btnContainer}
      buttonStyle={styles.btn}
      titleStyle={styles.btnLabel}
      onPress={() => {}}
    />
  </>
);

const Step3Content = () => (
  <>
    <View style={styles.imgContainer}>
      <Image source={step1Pic} style={styles.img} />
    </View>
    <Text style={styles.header}>Permite accesul la Bluetooth</Text>
    <Text style={styles.info}>
      Telefonul tău va putea comunica cu alte telefoane, transmițând informații
      pentru a ajuta la prevenirea răspândirii virusului
    </Text>
    <RoundedButton
      title="PERMITE ACCES"
      containerStyle={styles.btnContainer}
      buttonStyle={styles.btn}
      titleStyle={styles.btnLabel}
      onPress={() => {}}
    />
  </>
);

const Step4Content = () => (
  <>
    <View style={styles.imgContainer}>
      <Image source={step4Pic} style={styles.img} />
    </View>
    <Text style={styles.header}>Permite accesul la Notificări</Text>
    <Text style={styles.info}>
      Primește notificări despre riscul la expunere în zona ta. Fii la curent cu
      știrile importante legate de COVID-19
    </Text>
    <RoundedButton
      title="PERMITE ACCES"
      containerStyle={styles.btnContainer}
      buttonStyle={styles.btn}
      titleStyle={styles.btnLabel}
      onPress={() => {}}
    />
  </>
);

const Intro = ({ step = 1 }) => {
  let content = null;
  switch (step) {
    case 1:
      content = <Step1Content />;
      break;
    case 2:
      content = <Step2Content />;
      break;
    case 3:
      content = <Step3Content />;
      break;
    case 4:
      content = <Step4Content />;
      break;
  }

  return (
    <>
      <StatusBar translucent backgroundColor="transparent" />
      <ScrollView contentContainerStyle={styles.container}>
        <LinearGradient
          start={{ x: 1, y: 1 }}
          end={{ x: 0, y: 0 }}
          colors={['#0060a8', '#00c0ff']}
          style={styles.gradient}
        >
          {content}
          <DotsPagination stepsCount={4} step={step} />
        </LinearGradient>
      </ScrollView>
    </>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1
  },
  gradient: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-around',
    alignItems: 'center',
    padding: 16
  },
  imgContainer: {
    marginTop: 10,
    width: '80%',
    flexGrow: 1
  },
  img: {
    flex: 1,
    width: undefined,
    height: undefined,
    resizeMode: 'contain'
  },
  header: {
    marginTop: 35,
    marginBottom: 35,
    color: '#fff',
    fontSize: 22,
    fontWeight: 'bold',
    textAlign: 'center',
    textTransform: 'uppercase'
  },
  info: {
    marginTop: 16,
    color: '#fff',
    fontSize: 17,
    textAlign: 'center'
  },
  btn: {
    backgroundColor: '#fff'
  },
  btnLabel: {
    color: '#2c314c'
  },
  btnContainer: {
    width: '100%',
    marginTop: 16
  }
});

export default Intro;
