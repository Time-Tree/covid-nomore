import React, { useState } from 'react';
import { Image, Text, TextInput, StyleSheet } from 'react-native';
import {
  ScrollableScreenShell,
  RoundedCard,
  RoundedButton,
  ScreenHeader
} from '../../components';

import picSearchResult from './picSearchResult.png';

const Search = ({ navigation }) => {
  const [cnp, setCnp] = useState('');
  const [testNumber, setTestNumber] = useState('');

  return (
    <ScrollableScreenShell noPadding>
      <ScreenHeader title="My result" back={() => navigation.goBack()} />
      <RoundedCard style={styles.card}>
        <Text style={styles.header}>Search your result</Text>
        <Text style={styles.text}>
          You can check if your result is ready -{'\n'}just fill in these fields
          below:
        </Text>
        <Image
          source={picSearchResult}
          style={styles.img}
          resizeMode="contain"
        />
        <TextInput
          style={styles.input}
          onChangeText={val => setCnp(val)}
          value={cnp}
          placeholder="CNP last 6 digits"
          placeholderTextColor="#7F7F7F"
          contextMenuHidden={true}
          keyboardType="number-pad"
          maxLength={6}
        />
        <TextInput
          style={styles.input}
          onChangeText={val => setTestNumber(val)}
          value={testNumber}
          placeholder="Test number"
          placeholderTextColor="#7F7F7F"
          contextMenuHidden={true}
          keyboardType="number-pad"
        />
        <RoundedButton
          title="SEARCH"
          containerStyle={styles.btnContainer}
          buttonStyle={styles.btn}
          onPress={() => {}}
        />
      </RoundedCard>
    </ScrollableScreenShell>
  );
};

const styles = StyleSheet.create({
  card: {
    borderBottomLeftRadius: 0,
    borderBottomRightRadius: 0,
    paddingBottom: 36
  },
  header: {
    fontSize: 34,
    textAlign: 'center',
    marginTop: 24,
    color: '#2c314c'
  },
  text: {
    fontSize: 14,
    textAlign: 'center',
    marginTop: 18,
    color: '#282828'
  },
  img: {
    width: 296,
    height: 228,
    marginLeft: 'auto',
    marginRight: 'auto',
    marginTop: 22
  },
  input: {
    borderBottomColor: '#F4F4F4',
    borderBottomWidth: 1,
    marginTop: 22,
    marginHorizontal: 16,
    paddingVertical: 13,
    fontSize: 17
  },
  btnContainer: {
    marginTop: 26,
    marginLeft: 'auto',
    marginRight: 'auto'
  },
  btn: {
    backgroundColor: '#00c0ff'
  }
});

export default Search;
