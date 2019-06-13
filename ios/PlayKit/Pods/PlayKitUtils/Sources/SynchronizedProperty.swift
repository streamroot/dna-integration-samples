// ===================================================================================================
// Copyright (C) 2017 Kaltura Inc.
//
// Licensed under the AGPLv3 license, unless a different license for a 
// particular library is specified in the applicable library path.
//
// You may obtain a copy of the License at
// https://www.gnu.org/licenses/agpl-3.0.html
// ===================================================================================================


import Foundation

/// `SynchronizedProperty` will be used as a property with synchronized protection for getting and setting it value.
/// The value change observe will only fire on different value from current one, if the same value is received the value won't be set. 
/// ````
/// // creating the property
/// var state = SynchronizedProperty<MyState>(initialValue: .new)
///
/// // getting the value
/// state.value
///
/// // settings the value:
/// state.value = .newState
/// ````
public final class SynchronizedProperty<Value: Equatable> {
    
    private let synchronizedQueue: DispatchQueue
    private var _value: Value
    private var valueChangedHandler: ((Value) -> Void)?
    
    /// The value of the synchronized property
    public var value: Value {
        get {
            return synchronizedQueue.sync {
                return self._value
            }
        }
        set {
            synchronizedQueue.sync {
                if self._value != newValue {
                    self._value = newValue
                    self.valueChangedHandler?(newValue)
                }
            }
        }
    }
    
    public init(initialValue value: Value, synchornizedQueue: DispatchQueue? = nil) {
        self._value = value
        self.synchronizedQueue = synchornizedQueue == nil ? DispatchQueue(label: "\(Value.self)-synchornized") : synchornizedQueue!
    }
    
    
    /// observe the changes of the value on the selected dispatch queue.
    ///
    /// - Parameters:
    ///   - handler: the on change handler to be called when the value changes.
    public func onChange(handler: ((Value) -> Void)?) {
        self.valueChangedHandler = handler
    }
}
