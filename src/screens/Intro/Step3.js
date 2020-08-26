import React from 'react';
import { Image, View, Text } from 'react-native';
import { RoundedButton } from '../../components';
import Container, { styles } from './Container';
import step1Pic from './picIntro1.png';

const Step3 = ({ navigation }) => {
  const goBack = () => navigation.navigate('Step2');
  const goForward = () => navigation.navigate('Step4');

  return (
    <Container step={3} onGoBack={goBack} onGoForward={goForward}>
      <View style={styles.imgContainer}>
        <Image source={step1Pic} style={styles.img} />
      </View>
      <Text style={styles.header}>Permite accesul la Bluetooth</Text>
      <Text style={styles.info}>
        Telefonul tău va putea comunica cu alte telefoane, transmițând
        informații pentru a ajuta la prevenirea răspândirii virusului
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

export default Step3;
