import React from 'react';
import { Image, View, Text } from 'react-native';
import { RoundedButton } from '../../components';
import Container, { styles } from './Container';
import step1Pic from './picIntro1.png';

const Step3 = ({ navigation }) => (
  <Container step={3}>
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
  </Container>
);

export default Step3;
