import React, { useEffect, useReducer } from 'react';
import {
  Modal,
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  Platform
} from 'react-native';
import Slider from '@react-native-community/slider';
import NavbarComponent from './components/NavbarComponent';
import NearbyApi from '../utils/nearbyAPI';

const isAndroid = Platform.OS === 'android';

const initialState = {
  bleInterval: 5,
  bleDuration: 1,
  nearbyInterval: 5,
  nearbyDuration: 1
};

function reducer(state, action) {
  if (action.type === 'initialState') {
    return {
      ...action.value,
      initialbleDuration: action.value.bleDuration,
      initialnearbyDuration: action.value.nearbyDuration
    };
  }
  if (action.type === 'bleDuration' || action.type === 'nearbyDuration') {
    return {
      ...state,
      [action.type]: action.value,
      [`initial${action.type}`]: action.value
    };
  }
  return {
    ...state,
    [action.type]: action.value
  };
}

export default function SettingsModal(props) {
  const [state, dispatch] = useReducer(reducer, initialState);
  const inputs = {
    bleInterval: {
      name: 'BLE Interval',
      description:
        'The BLE Interval represents the number of minutes between two consecutive BLE processes.'
    },
    bleDuration: {
      name: 'BLE Duration',
      description:
        'The BLE duration is the duration of a BLE process expressed in minutes. (The duration of the BLE process must be less than or equal to the BLE interval.)'
    },
    nearbyInterval: {
      name: 'Nearby Interval',
      description:
        'The Neraby Interval represents the number of minutes between two consecutive nearby processes.'
    },
    nearbyDuration: {
      name: 'Nearby Duration',
      description:
        'The Nearby duration is the duration of a nearby process expressed in minutes. (The duration of the nearby process must be less than or equal to the nearby interval.)'
    }
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
    const data = { ...state };
    delete data.initialbleDuration;
    delete data.initialnearbyDuration;
    NearbyApi.saveSettings(data);
  };

  const renderSlider = type => (
    <View style={styles.sliderContainer}>
      <View style={styles.row}>
        <Text style={styles.title}>{inputs[type].name}:</Text>
        <View style={styles.valueContainer}>
          <Text style={styles.value}>{state[type]}</Text>
        </View>
      </View>
      <Slider
        minimumValue={1}
        maximumValue={maxValue(type)}
        step={1}
        minimumTrackTintColor="darkblue"
        thumbTintColor={isAndroid ? 'darkblue' : undefined}
        maximumTrackTintColor="lightgray"
        onValueChange={handler(type)}
        value={state[`initial${type}`] || props.settings[type]}
        style={styles.slider}
      />
      <View style={styles.interval}>
        <Text style={styles.intervalValues}>1</Text>
        <Text style={styles.intervalValues}>{maxValue(type)}</Text>
      </View>
      <View style={styles.descriptionContainter}>
        <Text style={styles.descriptionText}>{inputs[type].description}</Text>
      </View>
    </View>
  );

  return (
    <Modal
      visible={props.visible}
      animationType="slide"
      onRequestClose={props.visibleHandler}
      statusBarTranslucent
    >
      <View style={styles.container}>
        <NavbarComponent
          title="Settings"
          rightButton
          iconName="close"
          rightButtonHandler={props.visibleHandler}
        />
        <ScrollView style={styles.content}>
          {Object.keys(inputs).map(type => renderSlider(type))}
          <TouchableOpacity onPress={saveHandler} style={styles.saveButton}>
            <Text style={styles.saveText}>Save</Text>
          </TouchableOpacity>
        </ScrollView>
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
  slider: {
    height: 50
  },
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
    marginRight: 2,
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
  },
  descriptionContainter: {
    marginTop: 10,
    paddingBottom: 10,
    borderBottomColor: 'lightgray',
    borderBottomWidth: 0.5
  },
  descriptionText: {
    color: 'gray'
  }
});
