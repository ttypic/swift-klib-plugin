import Foundation
import KeychainAccess

@objc public class KeychainManager: NSObject {
    private let keychain = Keychain(service: "test-service")
}

@objc public class HelloWorld : NSObject {
    @objc public class func helloWorld() -> String {
        return "HeLLo WorLd!"
    }
}
