import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  headerContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginVertical: 10,
    marginHorizontal: 25
  },
  publishCode: {
    color: 'darkblue',
    fontWeight: 'bold'
  },
  eventContainer: {
    marginVertical: 10,
    marginHorizontal: 25
  },
  eventType: {
    fontWeight: 'bold'
  },
  eventMessage: {
    color: 'gray'
  }
});
