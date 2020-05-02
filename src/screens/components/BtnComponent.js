import React from 'react';
import { TouchableOpacity, Text, StyleSheet } from 'react-native';

export default function Btn(props) {
  let color = '#cadcfa';

  return (
    <TouchableOpacity
      onPress={props.onPress}
      style={[styles.btn, (props.active && styles.btnActive) || null]}
    >
      <Text>{props.title}</Text>
    </TouchableOpacity>
  );
}

export const styles = StyleSheet.create({
  btn: {
    alignItems: 'center',
    backgroundColor: '#DDDDDD',
    padding: 10,
    borderRadius: 5
  },
  btnActive: {
    backgroundColor: '#5c99ff'
  }
});
