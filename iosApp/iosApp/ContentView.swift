//
//  ContentView.swift
//  iosApp
//
//  Created by jiaochengyun on 2021/11/18.
//

import SwiftUI
import kmmshared

struct ContentView: View {
    var body: some View {
        Text(Greeting().greeting())
            .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
