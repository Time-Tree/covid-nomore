import React, { useEffect, useReducer } from 'react';
import { Modal, View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import Slider from '@react-native-community/slider';
import NavbarComponent from './components/NavbarComponent';
import NearbyApi from '../utils/nearbyAPI';

const initialState = {
  bleInterval: 5,
  bleDuration: 1,
  nearbyInterval: 5,
  nearbyDuration: 1
};

function reducer(state, action) {
  if (action.type === 'initialState') {
    return action.value;
  }
  return {
    ...state,
    [action.type]: action.value
  };
}

export default function SettingsModal(props) {
  const [state, dispatch] = useReducer(reducer, initialState);
  const inputs = {
    bleInterval: 'BLE Interval',
    bleDuration: 'BLE Duration',
    nearbyInterval: 'Nearby Interval',
    nearbyDuration: 'Nearby Duration'
  };

  useEffect(() => {
    dispatch({ type: 'initialState', value: props.settings });
  }, [props.settings]);

  const handler = type => value => {
    dispatch({ type, value });
    if (type.indexOf('Interval') > -1) {
      const method = type.substring(0, type.indexOf('Interval'));
      if (value < state[`${method}Duration`]) {
        dispatch({ type: `${method}Duration`, value });
      }
    }
  };

  const maxValue = label => {
    if (label.indexOf('Interval') > -1) {
      return 10;
    }
    const type = label.substring(0, label.indexOf('Duration'));
    return state[`${type}Interval`];
  };

  const saveHandler = () => {
    props.visibleHandler();
    NearbyApi.saveSettings(state);
  };

  const renderSlider = type => (
    <View style={styles.sliderContainer}>
      <View style={styles.row}>
        <Text style={styles.title}>{inputs[type]}:</Text>
        <View style={styles.valueContainer}>
          <Text style={styles.value}>{state[type]}</Text>
        </View>
      </View>
      <Slider
        minimumValue={1}
        maximumValue={maxValue(type)}
        step={1}
        minimumTrackTintColor="darkblue"
        maximumTrackTintColor="lightgray"
        onValueChange={handler(type)}
        value={state[type]}
        style={styles.slider}
      />
      <View style={styles.interval}>
        <Text style={styles.intervalValues}>1</Text>
        <Text style={styles.intervalValues}>{maxValue(type)}</Text>
      </View>
    </View>
  );

  return (
    <Modal
      visible={props.visible}
      animationType="slide"
      onRequestClose={props.visibleHandler}
    >
      <View style={styles.container}>
        <NavbarComponent
          title="Settings"
          rightButton
          iconName="close"
          rightButtonHandler={props.visibleHandler}
        />
        <View style={styles.content}>
          {Object.keys(inputs).map(type => renderSlider(type))}
          <TouchableOpacity onPress={saveHandler} style={styles.saveButton}>
            <Text style={styles.saveText}>Save</Text>
          </TouchableOpacity>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1
  },
  content: {
    paddingHorizontal: 25
  },
  sliderContainer: {
    marginVertical: 5
  },
  slider: {},
  row: {
    flexDirection: 'row',
    marginVertical: 10,
    alignItems: 'center',
    justifyContent: 'space-between'
  },
  title: {
    fontWeight: 'bold'
  },
  valueContainer: {
    borderRadius: 10,
    padding: 10,
    backgroundColor: '#f7f8fc',
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2
    },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5
  },
  value: {
    marginHorizontal: 10
  },
  interval: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginHorizontal: 10
  },
  intervalValues: {
    color: 'gray'
  },
  saveButton: {
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 25,
    margin: 50,
    paddingVertical: 15,
    paddingHorizontal: 10,
    borderColor: '#f7f8fc',
    backgroundColor: 'darkblue',
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2
    },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5
  },
  saveText: {
    color: 'white',
    fontWeight: '600'
  }
});
