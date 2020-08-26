import React from 'react';
import { Image, View, Text } from 'react-native';
import { RoundedButton } from '../../components';
import Container, { styles } from './Container';
import step1Pic from './picIntro1.png';

const Step2 = ({ navigation }) => {
  const goBack = () => navigation.navigate('Step1');
  const goForward = () => navigation.navigate('Step3');

  return (
    <Container step={2} onGoBack={goBack} onGoForward={goForward}>
      <View style={styles.imgContainer}>
        <Image source={step1Pic} style={styles.img} />
      </View>
      <Text style={styles.header}>Permite accesul la locație</Text>
      <Text style={styles.info}>
        Telefonul tău fa ști unde și cu cine ai intrat în contact și dacă
        această persoană a fost diagnosticată pozitiv
      </Text>
      <RoundedButton
        title="PERMITE ACCES"
        containerStyle={styles.btnContainer}
        buttonStyle={styles.btn}
        titleStyle={styles.btnLabel}
        onPress={() => {}}
      />
    </Container>
  );
}

export default Step2;
