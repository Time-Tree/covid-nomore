import React from 'react';
import { Image, View, Text } from 'react-native';
import { RoundedButton } from '../../components';
import Container, { styles } from './Container';
import step4Pic from './picIntro4.png';

const Step4 = ({ navigation }) => {
  const goBack = () => navigation.navigate('Step3');

  return (
    <Container step={4} onGoBack={goBack}>
      <View style={styles.imgContainer}>
        <Image source={step4Pic} style={styles.img} />
      </View>
      <Text style={styles.header}>Permite accesul la Notificări</Text>
      <Text style={styles.info}>
        Primește notificări despre riscul la expunere în zona ta. Fii la curent
        știrile importante legate de COVID-19
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
};

export default Step4;
