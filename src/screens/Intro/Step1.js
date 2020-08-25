import React from 'react';
import { Image, View, Text } from 'react-native';
import Container, { styles } from './Container';
import step1Pic from './picIntro1.png';

const Step1 = ({ navigation }) => (
  <Container step={1}>
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
  </Container>
);

export default Step1;
