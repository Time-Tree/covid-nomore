import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import FaIcon from 'react-native-vector-icons/FontAwesome';

const ScreenHeader = ({ title, back = null }) => {
  return (
    <View style={{ ...styles.header }}>
      <TouchableOpacity
        style={styles.btn}
        onPress={() => back !== null && back()}
      >
        <FaIcon name="angle-left" size={30} color="#008BCF" />
      </TouchableOpacity>
      <Text style={styles.title}>{title}</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  header: {
    height: 55,
    display: 'flex',
    flexDirection: 'row',
    alignItems: 'center'
  },
  btn: {
    width: 40,
    height: 40,
    paddingTop: 5,
    paddingBottom: 5,
    paddingLeft: 15,
    paddingRight: 15
  },
  title: {
    textAlign: 'center',
    marginRight: 40,
    flexGrow: 1,
    color: '#2c314c',
    fontSize: 17
  }
});

export default ScreenHeader;
