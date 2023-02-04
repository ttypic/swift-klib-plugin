import CryptoKit
import Foundation

@objc public class KCrypto : NSObject {
    @objc public class func md5(data: NSData) -> String {
        let hashed = Insecure.MD5.hash(data: data)
        return hashed.compactMap { String(format: "%02x", $0) }.joined()
    }
}
